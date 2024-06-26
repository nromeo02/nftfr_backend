package org.nftfr.backend.application;

import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.persistence.dao.UserDao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealTimeService {
    private record AuctionOffer(String nftId, Double value) {}
    private record AuctionUpdate(String event, String nftId, Double value) {}
    static private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> auctionEmitters = new ConcurrentHashMap<>();
    static private final ConcurrentLinkedQueue<AuctionOffer> auctionOffers = new ConcurrentLinkedQueue<>();

    private static void sendNewOfferToAll(AuctionOffer auctionOffer) {
        List<SseEmitter> emitters = auctionEmitters.get(auctionOffer.nftId());
        if (emitters == null)
            return;

        final AuctionUpdate update = new AuctionUpdate("newOffer", auctionOffer.nftId(), auctionOffer.value());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(update);
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private static void sendEndToAll(String nftId) {
        List<SseEmitter> emitters = auctionEmitters.get(nftId);
        if (emitters == null)
            return;

        final AuctionUpdate update = new AuctionUpdate("end", nftId, null);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(update);
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    public static void registerEmitter(String nftId, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            List<SseEmitter> emitters = auctionEmitters.get(nftId);
            if (emitters != null)
                emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            List<SseEmitter> emitters = auctionEmitters.get(nftId);
            if (emitters != null)
                emitters.remove(emitter);
        });

        CopyOnWriteArrayList<SseEmitter> emitters = auctionEmitters.computeIfAbsent(nftId, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);
    }

    public static void pushNewOffer(String nftId, Double value) {
        auctionOffers.add(new AuctionOffer(nftId, value));
    }

    // Send updates every 5 seconds.
    @Scheduled(fixedDelay = 5000)
    public void sendUpdates() {
        AuctionOffer offer = auctionOffers.poll();
        while (offer != null) {
            sendNewOfferToAll(offer);
            offer = auctionOffers.poll();
        }
    }

    // Check for auction end every 5 seconds.
    @Scheduled(fixedDelay = 5000)
    public void checkAuctionEnd() {
        final UserDao userDao = DBManager.getInstance().getUserDao();
        final SaleDao saleDao = DBManager.getInstance().getSaleDao();
        final LocalDateTime now = LocalDateTime.now();
        List<Sale> auctions = saleDao.getAllAuctions();

        for (Sale auction : auctions) {
            if (auction.getEndTime().isBefore(now)) {
                // Reset auction if no one made an offer.
                User offerMaker = userDao.findByUsername(auction.getOfferMaker());
                if (offerMaker == null) {
                    Duration between = Duration.between(auction.getCreationDate(), auction.getEndTime());
                    LocalDateTime newEnd = now.plus(between);
                    auction.setCreationDate(now);
                    auction.setEndTime(newEnd);
                    saleDao.update(auction);
                    continue;
                }

                // Transfer money.
                PaymentMethod sellerPM = auction.getSellerPaymentMethod();
                sellerPM.setBalance(sellerPM.getBalance() + auction.getPrice());

                // Transfer NFT ownership and update value.
                Nft nft = auction.getNft();
                nft.setOwner(offerMaker);
                nft.setValue(auction.getPrice());

                // Increase offer maker rank.
                offerMaker.setRank(offerMaker.getRank() + 1);

                // Update the database.
                DBManager.getInstance().beginTransaction();
                DBManager.getInstance().getPaymentMethodDao().update(sellerPM);
                DBManager.getInstance().getNftDao().update(nft);
                userDao.update(offerMaker);
                saleDao.remove(nft.getId());
                DBManager.getInstance().endTransaction();

                // Push end update to clients.
                sendEndToAll(nft.getId());
            }
        }
    }
}

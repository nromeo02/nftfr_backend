package org.nftfr.backend.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealTimeService {
    private record AuctionUpdate(String nftId, Double offer) {}
    static private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> auctionEmitters = new ConcurrentHashMap<>();
    static private final ConcurrentLinkedQueue<AuctionUpdate> auctionUpdates = new ConcurrentLinkedQueue<>();

    private static void sendUpdateToAll(String nftId, Double offer) {
        List<SseEmitter> emitters = auctionEmitters.get(nftId);
        if (emitters == null)
            return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(offer);
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

    public static void pushUpdate(String nftId, Double offer) {
        auctionUpdates.add(new AuctionUpdate(nftId, offer));
    }

    // Send updates every 5 seconds.
    @Scheduled(fixedDelay = 1000)
    public void sendUpdates() {
        AuctionUpdate update = auctionUpdates.poll();
        while (update != null) {
            sendUpdateToAll(update.nftId(), update.offer());
            update = auctionUpdates.poll();
        }
    }
    //fare metodo per end
}

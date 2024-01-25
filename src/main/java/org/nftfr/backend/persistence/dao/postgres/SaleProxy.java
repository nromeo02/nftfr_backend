package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleProxy extends Sale {
    private final Connection connection;
    private final String nftId;

    public SaleProxy(Connection connection, String nftId) {
        this.connection = connection;
        this.nftId = nftId;
    }

    @Override
    public Nft getNft() {
        Nft nft = super.getNft();

        if (nft == null) {
            final String sql = "SELECT n.* FROM nft n, sale s WHERE s.nft_id=? AND n.id = s.nft_id;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nftId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    nft = new NftProxy(connection);
                    nft.setId(rs.getString("id"));
                    nft.setCaption(rs.getString("caption"));
                    nft.setTitle(rs.getString("title"));
                    nft.setValue(rs.getDouble("value"));
                    nft.setTagsFromString(rs.getString("tags"));
                    super.setNft(nft);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return nft;
    }

    @Override
    public PaymentMethod getSellerPaymentMethod() {
        PaymentMethod paymentMethod = super.getSellerPaymentMethod();

        if (paymentMethod == null) {
            final String sql = "SELECT pm.* FROM payment_methods pm, sale s WHERE s.nft_id=? AND pm.address = s.destination_address;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nftId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    paymentMethod = new PaymentMethodProxy(connection);
                    paymentMethod.setAddress(rs.getString("address"));
                    paymentMethod.setType(rs.getInt("type"));
                    paymentMethod.setBalance(rs.getDouble("balance"));
                    super.setSellerPaymentMethod(paymentMethod);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return paymentMethod;
    }

    @Override
    public PaymentMethod getBuyerPaymentMethod() {
        PaymentMethod paymentMethod = super.getBuyerPaymentMethod();

        if (paymentMethod == null) {
            final String sql = "SELECT pm.* FROM payment_methods pm, sale s WHERE s.nft_id=? AND pm.address = s.destination_address;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nftId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    paymentMethod = new PaymentMethodProxy(connection);
                    paymentMethod.setAddress(rs.getString("address"));
                    paymentMethod.setType(rs.getInt("type"));
                    paymentMethod.setBalance(rs.getDouble("balance"));
                    super.setBuyerPaymentMethod(paymentMethod);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return paymentMethod;
    }
}

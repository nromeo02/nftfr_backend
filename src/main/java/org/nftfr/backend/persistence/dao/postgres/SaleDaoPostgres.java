package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDaoPostgres implements SaleDao {
    private final Connection connection;

    public SaleDaoPostgres(Connection connection) {
        this.connection = connection;
    }

    private Sale makeRSSale(ResultSet rs) throws SQLException {
        Sale sale = new SaleProxy(connection, rs.getString("nft_id"));
        sale.setPrice(rs.getDouble("price"));
        sale.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
        sale.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        sale.setOfferMaker(rs.getString("offer_maker"));
        return sale;
    }

    @Override
    public void add(Sale sale) {
        final String sql = "INSERT INTO sale (nft_id, seller_address, price, creation_date, end_time, offer_maker, buyer_address) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sale.getNft().getId());
            stmt.setString(2, sale.getSellerPaymentMethod().getAddress());
            stmt.setDouble(3, sale.getPrice());
            stmt.setObject(4, sale.getCreationDate());
            stmt.setObject(5, sale.getEndTime());
            stmt.setString(6, sale.getOfferMaker());
            if (sale.getBuyerPaymentMethod() != null) {
                stmt.setString(7, sale.getBuyerPaymentMethod().getAddress());
            } else {
                stmt.setString(7, null);
            }
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Sale sale) {
        final String sql = "UPDATE sale SET price=?, creation_date=?, end_time=?, offer_maker=?, buyer_address=? WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, sale.getPrice());
            stmt.setObject(2, sale.getCreationDate());
            stmt.setObject(3, sale.getEndTime());
            stmt.setString(4, sale.getOfferMaker());
            if (sale.getBuyerPaymentMethod() != null) {
                stmt.setString(5, sale.getBuyerPaymentMethod().getAddress());
            } else {
                stmt.setString(5, null);
            }
            stmt.setString(6, sale.getNft().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(String nftId) {
        final String sql = "DELETE FROM sale WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nftId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void removeByNftId(String nftId) {
        final String sql = "DELETE FROM sale WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nftId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Sale findByNftId(String nftId) {
        final String sql = "SELECT * FROM sale WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nftId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

           return makeRSSale(rs);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Sale> getAllSales() {
        final String sql = "SELECT * FROM sale WHERE end_time IS NULL;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            ArrayList<Sale> sales = new ArrayList<>();
            while (rs.next()) {
                sales.add(makeRSSale(rs));
            }

            return sales;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Sale> getAllAuctions() {
        final String sql = "SELECT * FROM sale WHERE end_time IS NOT NULL;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            ArrayList<Sale> auctions = new ArrayList<>();
            while (rs.next()) {
                auctions.add(makeRSSale(rs));
            }

            return auctions;
        } catch (SQLException ex) {
            throw  new RuntimeException(ex);
        }
    }
}

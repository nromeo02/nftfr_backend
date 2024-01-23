package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SaleDaoPostgres implements SaleDao {
    private final Connection connection;

    public SaleDaoPostgres(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(Sale sale) {
        final String sql = "INSERT INTO sale (nft_id, destination_address, price, creation_date, end_time) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sale.getNft().getId());
            stmt.setString(2, sale.getPaymentMethod().getAddress());
            stmt.setDouble(3, sale.getPrice());
            stmt.setObject(4, sale.getCreationDate());
            stmt.setObject(5, sale.getEndTime());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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

            Sale sale = new SaleProxy(connection, rs.getString("nft_id"));
            sale.setPrice(rs.getDouble("price"));
            sale.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
            sale.setEndTime(rs.getObject("end_time", LocalDateTime.class));
            return sale;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}

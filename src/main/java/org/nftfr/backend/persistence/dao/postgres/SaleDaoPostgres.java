package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.SaleIdBroker;
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
        final String sql = "INSERT INTO sale (id, nft_id, destination_address, price, creation_date, end_time) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, SaleIdBroker.getId(connection));
            stmt.setString(2, sale.getNft().getId());
            stmt.setString(3, sale.getPaymentMethod().getAddress());
            stmt.setDouble(4, sale.getPrice());
            stmt.setObject(5, sale.getCreationDate());
            stmt.setObject(6, sale.getEndTime());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void remove(Long id) {
        final String sql = "DELETE FROM sale WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Sale findById(Long id) {
        final String sql = "SELECT * FROM sale WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            Sale sale = new SaleProxy(connection);
            sale.setId(rs.getLong("id"));
            sale.setPrice(rs.getDouble("price"));
            sale.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
            sale.setEndTime(rs.getObject("end_time", LocalDateTime.class));
            return sale;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}

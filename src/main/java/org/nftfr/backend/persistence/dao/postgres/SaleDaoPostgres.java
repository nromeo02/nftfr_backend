package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.User;

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

    @Override
    public void add(Sale sale) {
        String query = "INSERT INTO sale (id, nft_id, price, creation_date, end_time) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setInt(1, sale.getId());
            st.setString(2, sale.getIdNft());
            st.setDouble(3, sale.getPrice());
            st.setObject(4, sale.getCreationDate());
            st.setObject(5, sale.getEndTime());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(int id) {
        String query = "DELETE FROM sale WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setInt(1, id);
            int rowsDeleted = st.executeUpdate();

            if (rowsDeleted == 0) {
                throw new RuntimeException("Nessun record trovato per la rimozione");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Sale> findByUser(String username) {

        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM sale WHERE nft_id IN (SELECT id FROM nft WHERE owner = ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, username);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String idNft = rs.getString("nft_id");
                double price = rs.getDouble("price");
                LocalDateTime creationDate = rs.getObject("creation_date", LocalDateTime.class);
                LocalDateTime timeLeft = rs.getObject("end_time", LocalDateTime.class);

                Sale sale = new Sale(id, idNft, price, timeLeft, creationDate);
                sales.add(sale);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sales;
    }
    @Override
    public List<Sale> findByPrice(double min, double max) {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM sale WHERE price BETWEEN ? AND ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setDouble(1, min);
            st.setDouble(2, max);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String idNft = rs.getString("nft_id");
                double price = rs.getDouble("price");
                LocalDateTime timeLeft = rs.getObject("end_time", LocalDateTime.class);
                LocalDateTime creationDate = rs.getObject("creation_date", LocalDateTime.class);

                Sale sale = new Sale(id, idNft, price, timeLeft, creationDate);
                sales.add(sale);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sales;
    }

    @Override
    public Sale findById(int id) {
        Sale sale = new Sale();
        String query = "SELECT * FROM sale WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, id);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String idNft = rs.getString("nft_id");
                double price = rs.getDouble("price");
                LocalDateTime creationDate = rs.getObject("creation_date", LocalDateTime.class);
                LocalDateTime endTime = rs.getObject("end_time", LocalDateTime.class);

                return new Sale(id, idNft, price, creationDate, endTime);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}

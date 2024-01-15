package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class PaymentMethodDaoPostgres implements PaymentMethodDao {
    private final Connection connection;

    public PaymentMethodDaoPostgres(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(PaymentMethod paymentMethod) {
        String query = "INSERT INTO payment_methods (address, username, type, balance) VALUES (?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(1, paymentMethod.getAddress());
            st.setString(2, paymentMethod.getUsername());
            st.setInt(3, paymentMethod.getType());
            st.setDouble(4, paymentMethod.getBalance());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String address) {
        String query = "DELETE FROM payment_methods WHERE address = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(1, address);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PaymentMethod> findByUsername(String username) {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        String query = "SELECT * FROM payment_methods WHERE username = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(2, username);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String address = rs.getString("address");
                int type = rs.getInt("type");
                double balance = rs.getDouble("balance");

                PaymentMethod paymentMethod = new PaymentMethod(address, username, type, balance);
                paymentMethods.add(paymentMethod);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return paymentMethods;
    }

    @Override
    public PaymentMethod findByAddress(String address) {
        String query = "SELECT * FROM payment_methods WHERE address = ?";
        PaymentMethod paymentMethod = new PaymentMethod();
        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(1, address);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                int type = rs.getInt("type");
                double balance = rs.getDouble("balance");

                paymentMethod.setAddress(address);
                paymentMethod.setUsername(username);
                paymentMethod.setType(type);
                paymentMethod.setBalance(balance);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return paymentMethod;
    }

    @Override
    public void update(PaymentMethod paymentMethod) {
        String query = "UPDATE payment_methods SET balance = ? WHERE address = ? AND username = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(1, paymentMethod.getAddress());
            st.setString(2, paymentMethod.getUsername());
            st.setDouble(3, paymentMethod.getBalance());

            int rowsUpdated = st.executeUpdate();

            if (rowsUpdated == 0) {
                throw new RuntimeException("Nessun record trovato per l'aggiornamento");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

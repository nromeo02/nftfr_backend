package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.model.PaymentMethod;

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
    public boolean add(PaymentMethod paymentMethod) {
        // Make sure the payment method is not used yet.
        if (findByAddress(paymentMethod.getAddress()) != null)
            return false;

        final String sql = "INSERT INTO payment_methods (address, username, type, balance) VALUES (?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, paymentMethod.getAddress());
            stmt.setString(2, paymentMethod.getUser().getUsername());
            stmt.setInt(3, paymentMethod.getType());
            stmt.setDouble(4, paymentMethod.getBalance());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(PaymentMethod paymentMethod) {
        final String sql = "UPDATE payment_methods SET balance=? WHERE address=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, paymentMethod.getBalance());
            stmt.setString(2, paymentMethod.getAddress());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String address) {
        final String sql = "DELETE FROM payment_methods WHERE address=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, address);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deleteByUsername(String username) {
        final String sql = "DELETE FROM payment_methods WHERE username=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PaymentMethod findByAddress(String address) {
        final String sql = "SELECT * FROM payment_methods WHERE address=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, address);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            PaymentMethod paymentMethod = new PaymentMethodProxy(connection);
            paymentMethod.setAddress(rs.getString("address"));
            paymentMethod.setType(rs.getInt("type"));
            paymentMethod.setBalance(rs.getDouble("balance"));
            return paymentMethod;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<PaymentMethod> findByUsername(String username) {
        final String sql = "SELECT * FROM payment_methods WHERE username=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            List<PaymentMethod> paymentMethods = new ArrayList<>();
            while (rs.next()) {
                PaymentMethod paymentMethod = new PaymentMethodProxy(connection);
                paymentMethod.setAddress(rs.getString("address"));
                paymentMethod.setType(rs.getInt("type"));
                paymentMethod.setBalance(rs.getDouble("balance"));
                paymentMethods.add(paymentMethod);
            }

            return paymentMethods;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}

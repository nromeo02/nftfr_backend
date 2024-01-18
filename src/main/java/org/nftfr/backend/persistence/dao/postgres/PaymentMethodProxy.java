package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentMethodProxy extends PaymentMethod {
    private final Connection connection;

    public PaymentMethodProxy(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User getUser() {
        User user = super.getUser();

        if (user == null) {
            final String sql = "SELECT u.* FROM users u, payment_methods pm WHERE pm.address=? AND pm.username = u.username;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, getAddress());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setEncryptedPw(rs.getString("encrypted_pw"));
                    user.setRank(rs.getInt("rank"));
                    user.setAdmin(rs.getBoolean("admin"));
                    super.setUser(user);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return user;
    }
}

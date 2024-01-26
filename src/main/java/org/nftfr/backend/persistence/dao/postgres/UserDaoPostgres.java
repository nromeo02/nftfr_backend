package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoPostgres implements UserDao {
    private final Connection connection;

    public UserDaoPostgres(Connection c) {
        connection = c;
    }

    @Override
    public boolean register(User user) {
        // Make sure the username is available.
        if (findByUsername(user.getUsername()) != null)
            return false;

        // Insert this user into the database.
        final String sql = "INSERT INTO users (username, name, surname, encrypted_pw) VALUES (?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getSurname());
            stmt.setString(4, user.getEncryptedPw());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(User user) {
        final String sql = "UPDATE users SET name=?, surname=?, encrypted_pw=?, rank=? WHERE username=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getSurname());
            stmt.setString(3, user.getEncryptedPw());
            stmt.setInt(4, user.getRank());
            stmt.setString(5, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String username) {
        final String sql = "DELETE FROM users WHERE username=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public User findByUsername(String username) {
        final String sql = "SELECT name, surname, encrypted_pw, rank, admin FROM users WHERE username=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            User user = new User();
            user.setUsername(username);
            user.setName(rs.getString("name"));
            user.setSurname(rs.getString("surname"));
            user.setEncryptedPw(rs.getString("encrypted_pw"));
            user.setRank(rs.getInt("rank"));
            user.setAdmin(rs.getBoolean("admin"));
            return user;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public double getUserValue(String username) {
        double value = 0.0;
        final String sql = "Select * nft where owner=?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, username);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    double nftPrice = resultSet.getDouble("value");
                    value += nftPrice;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    public int getRank(String username) {
        final String sql = "SELECT rank FROM users WHERE username=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Se l'utente è trovato, ottieni il rank dalla colonna "rank"
                    return resultSet.getInt("rank");
                } else {
                    // L'utente non è stato trovato
                    throw new RuntimeException("User not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

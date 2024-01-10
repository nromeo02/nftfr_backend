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
        String query = "INSERT INTO users (username, name, surname, password) VALUES (?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getSurname());
            stmt.setString(4, user.getPassword());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE user SET name=?, surname=?, password=?, rank=?, admin=? WHERE username=?;");
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getSurname());
            stmt.setString(3, user.getPassword());
            stmt.setInt(4, user.getRank());
            stmt.setBoolean(5, user.isAdmin());
            stmt.setString(6, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM user WHERE username=?;");
            stmt.setString(1, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public User findByUsername(String username) {
        String query = "SELECT name, surname, password, rank, admin FROM users WHERE username =?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            User user = new User();
            user.setUsername(username);
            user.setName(rs.getString("name"));
            user.setSurname(rs.getString("surname"));
            user.setPassword(rs.getString("password"));
            user.setRank(rs.getInt("rank"));
            user.setAdmin(rs.getBoolean("admin"));
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

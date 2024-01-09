package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDaoPostgres implements UserDao {
    private final Connection connection;

    public UserDaoPostgres(Connection c) {
        connection = c;
    }

    @Override
    public void register(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO user (username, name, surname, password) VALUES (?, ?, ?, ?);");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getSurname());
            stmt.setString(4, user.getPassword());
            stmt.executeUpdate();
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
    public void findByUsername(String username) {

    }
}

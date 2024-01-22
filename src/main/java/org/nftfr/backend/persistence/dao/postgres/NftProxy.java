package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.persistence.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NftProxy extends Nft {
    private final Connection connection;

    public NftProxy(Connection connection) {
        this.connection = connection;
    }

    private static User execQuery(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setName(rs.getString("name"));
            user.setSurname(rs.getString("surname"));
            user.setEncryptedPw(rs.getString("encrypted_pw"));
            user.setRank(rs.getInt("rank"));
            user.setAdmin(rs.getBoolean("admin"));
            return user;
        }

        return null;
    }

    @Override
    public User getAuthor() {
        User author = super.getAuthor();

        if (author == null) {
            final String sql = "SELECT u.* FROM nft n, users u WHERE n.id=? AND n.author = u.username;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, getId());
                author = execQuery(stmt);
                super.setAuthor(author);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return author;
    }

    @Override
    public User getOwner() {
        User owner = super.getOwner();
        if (owner == null) {
            final String sql = "SELECT u.* FROM nft n, users u WHERE n.id=? AND n.owner = u.username;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, getId());
                owner = execQuery(stmt);
                super.setOwner(owner);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return owner;
    }
}

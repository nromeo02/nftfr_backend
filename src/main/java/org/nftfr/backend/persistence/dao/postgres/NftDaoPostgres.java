package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NftDaoPostgres implements NftDao {
    private final Connection connection;
    public NftDaoPostgres(Connection conn){
        this.connection = conn;
    }

    private List<Nft> execListQuery(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        ArrayList<Nft> results = new ArrayList<>();
        while (rs.next())
            results.add(makeNftFromRS(rs));
        return results;
    }

    private String makeTagsString(ArrayList<String> tags) {
        ArrayList<String> lowered = new ArrayList<>();
        for (String tag : tags)
            lowered.add(tag.toLowerCase());
        return String.join(",", lowered);
    }

    private ArrayList<String> makeTagsList(String tagsString) {
        ArrayList<String> tags = new ArrayList<>();
        for (String tag : tagsString.split(","))
            tags.add(tag.trim());
        return tags;
    }

    private Nft makeNftFromRS(ResultSet rs) throws SQLException {
        Nft nft = new Nft();
        nft.setId(rs.getString("id"));
        nft.setAuthor(rs.getString("author"));
        nft.setOwner(rs.getString("owner"));
        nft.setCaption(rs.getString("caption"));
        nft.setTitle(rs.getString("title"));
        nft.setValue(rs.getDouble("value"));
        nft.setTag(makeTagsList(rs.getString("tags")));
        return nft;
    }

    @Override
    public void create(Nft nft) {
        String sql = "INSERT INTO nft (id, author, owner, caption, title, value, tags) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nft.getId());
            stmt.setString(2, nft.getAuthor());
            stmt.setString(3, nft.getOwner());
            stmt.setString(4, nft.getCaption());
            stmt.setString(5, nft.getTitle());
            stmt.setDouble(6, nft.getValue());
            stmt.setString(7, makeTagsString(nft.getTag()));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Nft nft) {
        String sql = "UPDATE nft SET author=?, owner=?, caption=?, title=?, value=?, tags=? WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, nft.getAuthor());
            stmt.setString(2, nft.getOwner());
            stmt.setString(3, nft.getCaption());
            stmt.setString(4, nft.getTitle());
            stmt.setDouble(5, nft.getValue());
            stmt.setString(6, makeTagsString(nft.getTag()));
            stmt.setString(7, nft.getId());
            stmt.executeUpdate();
        }
        catch(SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM nft WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1,id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Nft findByPrimaryKey(String id){
        String sql = "SELECT id, author, owner, caption, title, value, tags FROM nft WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? makeNftFromRS(rs) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByOwner(String username) {
        String sql = "SELECT id, author, owner, caption, title, value, tags FROM nft WHERE owner=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByAuthor(String username) {
        String sql = "SELECT id, author, owner, caption, title, value, tags FROM nft WHERE author=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByQuery(Set<String> tokens, double minPrice, double maxPrice) {
        StringBuilder sql = new StringBuilder();

        // Build sql statement.
        for (int i = 0; i < tokens.size(); ++i) {
            if (i != 0)
                sql.append(" UNION ");

            sql.append("(SELECT id, author, owner, caption, title, value, tags FROM nft WHERE " +
                    "((title LIKE ?) OR (caption LIKE ?) OR (tags LIKE ?)) AND (value BETWEEN ? AND ?))");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // Fill in parameters.
            int i = 1;
            for (String token : tokens) {
                String tokenString = "%" + token + "%";
                stmt.setString(i++, tokenString);
                stmt.setString(i++, tokenString);
                stmt.setString(i++, tokenString);
                stmt.setDouble(i++, minPrice);
                stmt.setDouble(i++, maxPrice);
            }

            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}


package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.DBManager;
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

    private Nft makeNftProxy(ResultSet rs) throws SQLException {
        Nft nft = new NftProxy(DBManager.getInstance().getConnection());
        nft.setId(rs.getString("id"));
        nft.setCaption(rs.getString("caption"));
        nft.setTitle(rs.getString("title"));
        nft.setValue(rs.getDouble("value"));
        nft.setTagsFromString(rs.getString("tags"));
        return nft;
    }

    private List<Nft> execListQuery(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        ArrayList<Nft> results = new ArrayList<>();
        while (rs.next())
            results.add(makeNftProxy(rs));
        return results;
    }

    @Override
    public void create(Nft nft) {
        final String sql = "INSERT INTO nft (id, author, owner, caption, title, value, tags) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nft.getId());
            stmt.setString(2, nft.getAuthor().getUsername());
            stmt.setString(3, nft.getOwner().getUsername());
            stmt.setString(4, nft.getCaption());
            stmt.setString(5, nft.getTitle());
            stmt.setDouble(6, nft.getValue());
            stmt.setString(7, nft.getTagsAsString());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Nft nft) {
        final String sql = "UPDATE nft SET author=?, owner=?, caption=?, title=?, value=?, tags=? WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, nft.getAuthor().getUsername());
            stmt.setString(2, nft.getOwner().getUsername());
            stmt.setString(3, nft.getCaption());
            stmt.setString(4, nft.getTitle());
            stmt.setDouble(5, nft.getValue());
            stmt.setString(6, nft.getTagsAsString());
            stmt.setString(7, nft.getId());
            stmt.executeUpdate();
        }
        catch(SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String id) {
        final String sql = "DELETE FROM nft WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void clearAllAuthors(String username) {
        final String sql = "UPDATE nft SET author=NULL WHERE author=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Nft findById(String id){
        final String sql = "SELECT * FROM nft WHERE id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? makeNftProxy(rs) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByOwner(String username) {
        final String sql = "SELECT * FROM nft WHERE owner=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByAuthor(String username) {
        final String sql = "SELECT * FROM nft WHERE author=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Nft> findByQuery(Set<String> tokens, double minPrice, double maxPrice, boolean onSale) {
        StringBuilder sql = new StringBuilder();

        if (!tokens.isEmpty()) {
            // Build sql statement with tokens.
            for (int i = 0; i < tokens.size(); ++i) {
                if (i != 0)
                    sql.append(" UNION ");

                if (onSale) {
                    sql.append("(SELECT n.* FROM nft n, sale s WHERE n.id = s.nft_id AND ((title LIKE ?) OR (caption LIKE ?) OR (tags LIKE ?)) AND (value BETWEEN ? AND ?))");
                } else {
                    sql.append("(SELECT * FROM nft WHERE ((title LIKE ?) OR (caption LIKE ?) OR (tags LIKE ?)) AND (value BETWEEN ? AND ?))");
                }
            }
        } else {
            // Build sql statement without tokens.
            sql.append("SELECT n.* FROM nft n, sale s WHERE (value BETWEEN ? AND ?)");
            if (onSale)
                sql.append(" AND s.nft_id = n.id");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // Fill in parameters.
            if (!tokens.isEmpty()) {
                int i = 1;
                for (String token : tokens) {
                    String tokenString = "%" + token + "%";
                    stmt.setString(i++, tokenString);
                    stmt.setString(i++, tokenString);
                    stmt.setString(i++, tokenString);
                    stmt.setDouble(i++, minPrice);
                    stmt.setDouble(i++, maxPrice);
                }
            } else {
                stmt.setDouble(1, minPrice);
                stmt.setDouble(2, maxPrice);
            }

            return execListQuery(stmt);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}


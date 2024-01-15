package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NftDaoPostgres implements NftDao {
    private final Connection connection;
    public NftDaoPostgres(Connection conn){
        this.connection = conn;
    }
    @Override
    public Nft findByPrimaryKey(String id){
        Nft nft = null;
        String query = "select * from nft where id = ?";
        try{
            PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()){
                nft = new Nft();
                nft.setId(rs.getString("id"));
                nft.setAuthor(rs.getString("author"));
                nft.setOwner(rs.getString("owner"));
                nft.setCaption(rs.getString("caption"));
                nft.setTitle(rs.getString("title"));
                nft.setValue(rs.getDouble("value"));

                String tagsString = rs.getString("tags");
                ArrayList<String> tags = new ArrayList<>();
                String[] tagArray = tagsString.split(",");
                for (String tag : tagArray) {
                    tags.add(tag.trim());
                }
                nft.setTag(tags);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return nft;

    }

    @Override
    public void create(Nft nft) {
        String query = "INSERT INTO nft (id, author, owner, caption, title, value, tags) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setString(1, nft.getId());
            st.setString(2, nft.getAuthor());
            st.setString(3, nft.getOwner());
            st.setString(4, nft.getCaption());
            st.setString(5, nft.getTitle());
            st.setDouble(6, nft.getValue());
            st.setString(7, String.join(",", nft.getTag()));
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Nft nft) {
        String query = "UPDATE nft SET author=?, owner=?, caption=?, title=?, value=?, tags=? WHERE id=?";

        try (PreparedStatement st = connection.prepareStatement(query)){

            st.setString(1, nft.getAuthor());
            st.setString(2, nft.getOwner());
            st.setString(3, nft.getCaption());
            st.setString(4, nft.getTitle());
            st.setDouble(5, nft.getValue());
            st.setString(6, String.join(",", nft.getTag()));
            st.setString(7, nft.getId());

            st.executeUpdate();
            }
            catch(SQLException e){
                throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String query = "delete from nft where id = ?";
        try{
            PreparedStatement st = connection.prepareStatement(query);
            st.setString(1,id);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Nft> findByQuery(List<String> search) {
        List<Nft> nfts = new ArrayList<>();
        //String query = "select * from nft where  "
        return null;
    }

    @Override
    public List<Nft> findByTag(List<String> tag) {
        List<Nft> nfts = new ArrayList<>();
        String query = "select * from nft where tags LIKE ?";
        try{
            PreparedStatement st = connection.prepareStatement(query);
            for (String tags : tag){
                st.setString(1, "%" + tag + "%");
                ResultSet rs = st.executeQuery();

                while(rs.next()){
                    Nft nft = new Nft();
                    nft.setId(rs.getString("id"));
                    nft.setAuthor(rs.getString("author"));
                    nft.setOwner(rs.getString("owner"));
                    nft.setCaption(rs.getString("caption"));
                    nft.setTitle(rs.getString("title"));
                    nft.setValue(rs.getDouble("value"));
                    ArrayList<String> nftTags = new ArrayList<>();
                    String tagsString = rs.getString("tag");
                    String[] tagArray = tagsString.split(",");
                    for (String t : tagArray) {
                        nftTags.add(t.trim());
                    }
                    nft.setTag(nftTags);

                    nfts.add(nft);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return nfts;
    }

    @Override
    public List<Nft> findByValue(int max, int min) {

        List<Nft> nftsByValue = new ArrayList<>();
        String query = "SELECT * FROM nft WHERE value >= ? AND value <= ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {

            st.setDouble(1, min);
            st.setDouble(2, max);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Nft nft = new Nft();
                nft.setId(rs.getString("id"));
                nft.setAuthor(rs.getString("author"));
                nft.setOwner(rs.getString("owner"));
                nft.setCaption(rs.getString("caption"));
                nft.setTitle(rs.getString("title"));
                nft.setValue(rs.getDouble("value"));

                ArrayList<String> tags = new ArrayList<>();
                String tagsString = rs.getString("tag");
                String[] tagArray = tagsString.split(",");
                for (String tag : tagArray) {
                    tags.add(tag.trim());
                }
                nft.setTag(tags);

                nftsByValue.add(nft);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nftsByValue;
    }
}


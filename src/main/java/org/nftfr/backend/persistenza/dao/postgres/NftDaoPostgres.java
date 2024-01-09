package org.nftfr.backend.persistenza.dao.postgres;

import org.nftfr.backend.persistenza.dao.NftDao;
import org.nftfr.backend.persistenza.model.Nft;

import java.sql.Connection;
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


    }

    @Override
    public void create(Nft nft) {

    }

    @Override
    public void update(Nft nft) {

    }

    @Override
    public void delete(Nft nft) {

    }

    @Override
    public List<Nft> findByQuery(List<String> search) {
        return null;
    }

    @Override
    public List<Nft> findByTag(List<String> tag) {
        return null;
    }

    @Override
    public List<Nft> findByValue(int max, int min) {
        return null;
    }
}

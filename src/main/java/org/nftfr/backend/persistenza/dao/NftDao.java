package org.nftfr.backend.persistenza.dao;

import org.nftfr.backend.persistenza.model.Nft;

import java.util.List;

public interface NftDao {
    public Nft findByPrimaryKey(String id);
    public void create(Nft nft);
    public void update (Nft nft);
    public void delete (Nft nft);
    public List<Nft> findByQuery(List<String> search);
    public List<Nft> findByTag(List<String> tag);
    public List<Nft> findByValue(int max, int min);
    //range per cui cercare gli nft o magari si puo anche fare maggiore di max o minore di min senza range
}

package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Nft;

import java.util.List;
import java.util.Set;

public interface NftDao {
    void create(Nft nft);
    void update(Nft nft);
    void delete(String id);
    Nft findById(String id);
    List<Nft> findByOwner(String username);
    List<Nft> findByAuthor(String username);
    List<Nft> findByQuery(Set<String> tokens, double minPrice, double maxPrice);
    void report(String id);
}
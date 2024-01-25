package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Sale;

import java.util.List;

public interface SaleDao {
    void add(Sale sale);
    void update(Sale sale);
    void remove(String nftId);
    void removeByNftId(String nftId);
    Sale findByNftId(String nftId);
    List<Sale> getAllAuctions();
}

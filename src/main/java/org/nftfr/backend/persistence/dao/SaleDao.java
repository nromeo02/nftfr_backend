package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Sale;
import org.nftfr.backend.persistence.model.User;

import java.util.List;

public interface SaleDao {
    void add(Sale sale);
    void remove(int id);
    List<Sale> findByUser(String username);
    //List<Sale> findMoreRecent();
    List<Sale> findByPrice(double min, double max);
}

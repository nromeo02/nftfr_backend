package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Sale;

public interface SaleDao {
    void add(Sale sale);
    void remove(Long id);
    Sale findById(Long id);
}

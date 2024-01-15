package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.User;

import java.util.List;

public interface PaymentMethodDao {
    void add(PaymentMethod paymentMethod);
    void delete(String address);
    List<PaymentMethod> findByUsername(String username);
    PaymentMethod findByAddress(String address);
    void update(PaymentMethod paymentMethod);//per il balance anche se cambia tutto nella query

}

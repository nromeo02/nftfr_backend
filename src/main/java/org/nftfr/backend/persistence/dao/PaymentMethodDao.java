package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.PaymentMethod;

import java.util.List;

public interface PaymentMethodDao {
    // Returns true if the payment method has been added, or false if it already existed.
    boolean add(PaymentMethod paymentMethod);
    void update(PaymentMethod paymentMethod);
    void delete(String address);
    PaymentMethod findByAddress(String address);
    List<PaymentMethod> findByUsername(String username);
}

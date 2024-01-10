package org.nftfr.backend.persistence.dao.postgres;

import java.sql.Connection;

public class PaymentMethodDaoPostgres {
    private final Connection connection;

    public PaymentMethodDaoPostgres(Connection connection) {
        this.connection = connection;
    }
}

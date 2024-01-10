package org.nftfr.backend.persistence.dao.postgres;

import java.sql.Connection;

public class SaleDaoPostgres {
    private final Connection connection;

    public SaleDaoPostgres(Connection connection) {
        this.connection = connection;
    }
}

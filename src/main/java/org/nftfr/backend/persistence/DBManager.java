package org.nftfr.backend.persistence;

import org.nftfr.backend.ConfigManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.dao.postgres.NftDaoPostgres;
import org.nftfr.backend.persistence.dao.postgres.SaleDaoPostgres;
import org.nftfr.backend.persistence.dao.postgres.UserDaoPostgres;
import org.nftfr.backend.persistence.dao.postgres.PaymentMethodDaoPostgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static DBManager instance = null;
    private Connection connection = null;

    private DBManager() {}

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();

        return instance;
    }

    public Connection getConnection() {
        if (connection == null) {
            ConfigManager config = ConfigManager.getInstance();
            final String url = "jdbc:postgresql://localhost:" + config.getDBPort() + "/" + config.getDBName();
            try {
                connection = DriverManager.getConnection(url, config.getDBUsername(), config.getDBPassword());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return connection;
    }

    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void endTransaction() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NftDao getNftDao() { return new NftDaoPostgres(getConnection()); }
    public PaymentMethodDao getPaymentMethodDao() { return new PaymentMethodDaoPostgres(getConnection());}
    public UserDao getUserDao() { return new UserDaoPostgres(getConnection()); }
    public SaleDao getSaleDao() { return new SaleDaoPostgres(getConnection()); }

}

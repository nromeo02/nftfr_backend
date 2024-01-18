package org.nftfr.backend.persistence;

import org.nftfr.backend.ConfigManager;
import org.nftfr.backend.persistence.dao.*;
import org.nftfr.backend.persistence.dao.postgres.*;

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

    public NftDao getNftDao() { return new NftDaoPostgres(getConnection()); }
    public PaymentMethodDao getPaymentMethodDao() { return new PaymentMethodDaoPostgres(getConnection());}
    public UserDao getUserDao() { return new UserDaoPostgres(getConnection()); }
    public SaleDao getSaleDao() { return new SaleDaoPostgres(getConnection()); }
    public ReportDao getReportDao() { return new ReportDaoPostgres(getConnection());
    }

}

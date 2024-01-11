package org.nftfr.backend.persistence;

import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.dao.postgres.NftDaoPostgres;
import org.nftfr.backend.persistence.dao.postgres.UserDaoPostgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String POSTGRES_PORT = "5432";
    private static final String DB_NAME = "nftfr";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "";

    private static DBManager instance = null;
    private Connection connection = null;

    private DBManager() {}

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();

        return instance;
    }

    private String getDBURL() {
        return "jdbc:postgresql://localhost:" + POSTGRES_PORT + "/" + DB_NAME;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(getDBURL(), USERNAME, PASSWORD);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return connection;
    }

    public NftDao getNftDao() { return new NftDaoPostgres(getConnection()); }
    // public PaymentMethodDao getPaymentDao() { return new PaymentMethodDaoPostgres(getConnection());}
    // public SaleDao getSaleDao() { return new SaleDaoPostgres(getConnection()); }
    public UserDao getUserDao() { return new UserDaoPostgres(getConnection()); }
}

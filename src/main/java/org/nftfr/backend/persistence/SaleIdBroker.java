package org.nftfr.backend.persistence;

import org.nftfr.backend.ConfigManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleIdBroker {
    private static final String SQL = ConfigManager.getInstance().getSaleIdBrokerSQL();

    public static Long getId(Connection connection) {
        long id;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL);
            ResultSet result = statement.executeQuery();
            result.next();
            id = result.getLong("id");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return id;
    }
}
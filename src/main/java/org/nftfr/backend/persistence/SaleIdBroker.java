package org.nftfr.backend.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleIdBroker {
    private static final String SQL = "select nextval('sale_sequence') as id;";

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
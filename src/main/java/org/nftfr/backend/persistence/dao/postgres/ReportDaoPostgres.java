package org.nftfr.backend.persistence.dao.postgres;

import org.nftfr.backend.persistence.dao.ReportDao;
import org.nftfr.backend.persistence.model.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoPostgres implements ReportDao {
    private final Connection connection;
    private ReportDaoPostgres reportDao;

    public ReportDaoPostgres(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createorUpdatereport(String id) {
        Report report = reportDao.getReportById(id);
        if (report == null){
            String insert = "INSERT INTO reported (nft_id, counter) VALUES (?, 1)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insert)) {
                insertStmt.setString(1, id);
                insertStmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else{
            String update = "UPDATE reported SET counter = counter + 1 WHERE nft_id = ?";

            try (PreparedStatement updateStmt = connection.prepareStatement(update)) {
                // Set the parameters for the prepared statement
                updateStmt.setString(1, id);

                // Execute the update
                updateStmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Report> getReports() {
        String query = "SELECT * FROM reported";
        List<Report> reports = new ArrayList<>();
        try (PreparedStatement selectStmt = connection.prepareStatement(query)) {
            try (ResultSet resultSet = selectStmt.executeQuery()) {
                while (resultSet.next()) {
                    String nftId = resultSet.getString("nft_id");
                    int counter = resultSet.getInt("counter");
                    Report report = new Report(nftId, counter);
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public Report getReportById(String id) {
        String select = "SELECT * FROM reported WHERE nft_id = ?";
        Report report = null;
        try (PreparedStatement selectStmt = connection.prepareStatement(select)) {
            selectStmt.setString(1, id);
            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    String nftId = resultSet.getString("nft_id");
                    int counter = resultSet.getInt("counter");
                    report.setNft_id(nftId);
                    report.setCounter(counter);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return report;
    }
}

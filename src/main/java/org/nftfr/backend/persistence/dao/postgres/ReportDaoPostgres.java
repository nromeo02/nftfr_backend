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

    public ReportDaoPostgres(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdateReport(String id) {
        Report report = getReportById(id);

        String sql;
        if (report == null) {
            sql = "INSERT INTO reported (nft_id, counter) VALUES (?, 1);";
        } else {
            sql = "UPDATE reported SET counter = counter + 1 WHERE nft_id=?;";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String id) {
        final String sql = "DELETE FROM reported WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Report> getReports() {
        final String sql = "SELECT * FROM reported;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            ArrayList<Report> reports = new ArrayList<>();
            while (rs.next()) {
                Report report = new Report();
                report.setNftId(rs.getString("nft_id"));
                report.setCounter(rs.getInt("counter"));
                reports.add(report);
            }
            return reports;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Report getReportById(String id) {
        final String sql = "SELECT * FROM reported WHERE nft_id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            Report report = new Report();
            report.setNftId(rs.getString("nft_id"));
            report.setCounter(rs.getInt("counter"));
            return report;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

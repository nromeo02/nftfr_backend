package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Report;

import java.util.List;

public interface ReportDao {
    void createOrUpdateReport(String id, String message);
    void delete(String id);
    List<Report> getReports();
    Report getReportById(String id);
}

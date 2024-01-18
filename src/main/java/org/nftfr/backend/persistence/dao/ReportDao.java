package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.Report;

import java.util.List;

public interface ReportDao {
    void createorUpdatereport(String id);
    List<Report> getReports();
    Report getReportById(String id);
}

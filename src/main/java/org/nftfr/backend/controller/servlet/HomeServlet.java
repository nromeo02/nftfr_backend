package org.nftfr.backend.controller.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.model.Report;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Check if the user is authenticated.
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect("/admin/login");
            return;
        }

        // Get list of reports.
        List<Report> reports = DBManager.getInstance().getReportDao().getReports();
        req.setAttribute("username", session.getAttribute("username"));
        req.setAttribute("reports_list", reports);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/views/admin_home.html");
        dispatcher.forward(req, res);
    }
}
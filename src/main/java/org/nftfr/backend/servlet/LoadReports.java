package org.nftfr.backend.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.model.Report;

import java.io.IOException;
import java.util.List;

@WebServlet("/load/admin")
public class LoadReports extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Report> reports = DBManager.getInstance().getReportDao().getReports();
        for (Report r : reports) {
            System.out.println(r.getNft_id());
            System.out.println(r.getCounter());
        }
     /*   String fullPath = req.getServletContext().getRealPath("/WEB-INF/classes/templates/admin.html");
        System.out.println("Percorso completo: " + fullPath);

        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/classes/templates/admin.html");
        dispatcher.forward(req, resp);
      */
        req.setAttribute("lista_reports", reports);

        RequestDispatcher dispatcher = req.getRequestDispatcher("/views/admin.html");
        dispatcher.forward(req, resp);



    }
}
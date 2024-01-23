package org.nftfr.backend.controller.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.SaleDao;
import org.nftfr.backend.persistence.model.Sale;

import java.io.IOException;

@WebServlet("/admin/delete/sale")
public class DeleteSaleServlet extends HttpServlet {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();

    private void reloadPage(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/views/admin_operation.html");
        requestDispatcher.forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/admin/login");
            return;
        }

        // Check if the sale exists.
        final String nftId = req.getParameter("saleNftId");
        Sale sale = saleDao.findByNftId(nftId);
        if (sale == null) {
            req.setAttribute("error_msg", "questa vendita non esiste");
            reloadPage(req, res);
            return;
        }

        saleDao.remove(nftId);
        reloadPage(req, res);
    }
}

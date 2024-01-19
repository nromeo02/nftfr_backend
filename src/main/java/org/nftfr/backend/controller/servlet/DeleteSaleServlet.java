package org.nftfr.backend.controller.servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.SaleDao;
import java.io.IOException;
@WebServlet("/admin/delete/sale")
public class DeleteSaleServlet extends HttpServlet {
    private final SaleDao saleDao = DBManager.getInstance().getSaleDao();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/admin/login");
            return;
        }
        final Long saleId = Long.parseLong(req.getParameter("saleId"));
        saleDao.remove(saleId);
        res.sendRedirect("/admin");
    }
}

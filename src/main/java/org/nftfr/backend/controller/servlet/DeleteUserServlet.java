package org.nftfr.backend.controller.servlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;

import java.io.IOException;
@WebServlet("/admin/delete/user")
public class DeleteUserServlet extends HttpServlet {
    private final UserDao userDao = DBManager.getInstance().getUserDao();

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

        // Check if user exists.
        final String targetUsername = req.getParameter("targetUsername");
        User user = userDao.findByUsername(targetUsername);
        if (user == null) {
            req.setAttribute("error_msg", "questo utente non esiste");
            reloadPage(req, res);
            return;
        }

        // Block deletion if this user has any NFT.
        NftDao nftDao = DBManager.getInstance().getNftDao();
        if (!nftDao.findByOwner(targetUsername).isEmpty()) {
            req.setAttribute("error_msg", "questo utente possiede NFT e non può essere eliminato");
            reloadPage(req, res);
            return;
        }

        // Remove all payment methods and nullify all nfts author fields, then remove the user.
        DBManager.getInstance().beginTransaction();
        DBManager.getInstance().getPaymentMethodDao().deleteByUsername(targetUsername);
        nftDao.clearAllAuthors(targetUsername);
        userDao.delete(targetUsername);
        DBManager.getInstance().endTransaction();
        reloadPage(req, res);
    }
}

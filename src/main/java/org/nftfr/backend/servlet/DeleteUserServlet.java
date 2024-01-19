package org.nftfr.backend.servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;

import java.io.IOException;
@WebServlet("/admin/delete/user")
public class DeleteUserServlet extends HttpServlet {
    private final UserDao userDao = DBManager.getInstance().getUserDao();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/admin/login");
            return;
        }
        final String targetUsername = req.getParameter("targetUsername");
        userDao.delete(targetUsername);
        res.sendRedirect("/admin");
    }
}

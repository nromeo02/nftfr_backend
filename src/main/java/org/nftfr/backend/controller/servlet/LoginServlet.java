package org.nftfr.backend.controller.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;

import java.io.IOException;

@WebServlet("/admin/login")
public class LoginServlet extends HttpServlet {
    private static final int ERROR_NOT_FOUND = 1;
    private static final int ERROR_AUTH_FAILED = 2;
    private static final int ERROR_NO_PERM = 3;
    private final UserDao userDao = DBManager.getInstance().getUserDao();

    private void reloadPage(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/views/admin_login.html");
        requestDispatcher.forward(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        reloadPage(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String username = req.getParameter("username");
        final String password = req.getParameter("password");

        if (username.isEmpty() && password.isEmpty()) {
            reloadPage(req, res);
            return;
        }

        // Get user.
        User user = userDao.findByUsername(username);
        if (user == null) {
            req.setAttribute("error_code", ERROR_NOT_FOUND);
            reloadPage(req, res);
            return;
        }

        if (!user.verifyPassword(password)) {
            req.setAttribute("error_code", ERROR_AUTH_FAILED);
            reloadPage(req, res);
            return;
        }

        if (!user.isAdmin()) {
            req.setAttribute("error_code", ERROR_NO_PERM);
            reloadPage(req, res);
            return;
        }

        // Create new session.
        HttpSession session = req.getSession();
        session.setAttribute("username", username);
        res.sendRedirect("/admin");
    }
}

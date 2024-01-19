package org.nftfr.backend.servlet;

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
    private final UserDao userDao = DBManager.getInstance().getUserDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        final String username = req.getParameter("username");
        final String password = req.getParameter("password");

        // Get user.
        User user = userDao.findByUsername(username);
        if (user == null) {
            // TODO
            System.out.println("User not found");
            return;
        }

        if (!user.verifyPassword(password)) {
            // TODO
            System.out.println("Authentication failed");
            return;
        }

        if (!user.isAdmin()) {
            // TODO
            System.out.println("Invalid permission");
            return;
        }

        // Create new session.
        HttpSession session = req.getSession();
        session.setAttribute("username", username);
        res.sendRedirect("/admin");
    }
}

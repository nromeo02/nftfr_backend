package org.nftfr.backend.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;

@WebServlet("/admin/delete/nft")
public class DeleteNftServlet extends HttpServlet {
    private final NftDao nftDao = DBManager.getInstance().getNftDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            // TODO
            System.out.println("Not authenticated");
            return;
        }

        final String nftId = req.getParameter("nftId");
        nftDao.delete(nftId);

        // TODO: redirect
    }
}

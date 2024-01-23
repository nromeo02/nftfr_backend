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
import org.nftfr.backend.persistence.model.Nft;
import org.nftfr.backend.utility.NftImage;

import java.io.IOException;
@WebServlet("/admin/delete/nft")
public class DeleteNftServlet extends HttpServlet {
    private final NftDao nftDao = DBManager.getInstance().getNftDao();

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

        // Check if NFT exists.
        final String nftId = req.getParameter("nftId");
        Nft nft = nftDao.findById(nftId);
        if (nft == null) {
            req.setAttribute("error_msg", "questo NFT non esiste");
            reloadPage(req, res);
            return;
        }

        // Delete all reports and sales related to this NFT, then delete the NFT.
        DBManager.getInstance().beginTransaction();
        DBManager.getInstance().getReportDao().delete(nftId);
        DBManager.getInstance().getSaleDao().removeByNftId(nftId);
        nftDao.delete(nftId);
        DBManager.getInstance().endTransaction();

        // Delete NFT image.
        NftImage.deleteWithId(nftId);
        reloadPage(req, res);
    }
}

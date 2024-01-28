package org.nftfr.backend.controller.rest;

import org.springframework.ui.Model;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.NftDao;
import org.nftfr.backend.persistence.model.Nft;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/admin/view")
public class ViewReportedNftRest {
   private final NftDao nftDao = DBManager.getInstance().getNftDao();

   @GetMapping("/nft/{id}")
   public ModelAndView viewNft(@PathVariable String id, Model model) {
       Nft nft = nftDao.findById(id);

       // Aggiungi l'oggetto Nft al modello per renderlo disponibile nella vista Thymeleaf
       model.addAttribute("nft", nft);

       // Ritorna il nome della vista Thymeleaf (senza l'estensione .html)
       return new ModelAndView("admin_view_nft");
   }
}

package org.nftfr.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserRest {
    private record RegisterBody(String username, String name, String surname, String password) {
        public User asUser() {
            User user = new User();
            user.setUsername(username);
            user.setName(name);
            user.setSurname(surname);
            user.setPassword(password);
            user.setRank(0);
            user.setAdmin(false);
            return user;
        }
    }
    @PostMapping(value = "/user/register")
    public void register(@RequestBody RegisterBody regBody, HttpServletResponse res) {
        UserDao userDao = DBManager.getInstance().getUserDao();
        if (!userDao.register(regBody.asUser()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");

        res.setStatus(HttpStatus.NO_CONTENT.value());
    }
}

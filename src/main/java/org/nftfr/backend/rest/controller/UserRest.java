package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.BasicToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UserRest {
    private final UserDao userDao = DBManager.getInstance().getUserDao();

    private record RegisterParams(String username, String name, String surname, String password) {
        public User asUser() {
            User user = new User();
            user.setUsername(username);
            user.setName(name);
            user.setSurname(surname);
            user.setEncryptedPw(User.encryptPassword(password));
            return user;
        }
    }

    @PostMapping(value = "/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@RequestBody RegisterParams params, HttpServletResponse res) {
        if (!userDao.register(params.asUser()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
    }

    @PostMapping(value = "/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthToken login(HttpServletRequest req, HttpServletResponse res) {
        // Get basic token.
        BasicToken token = BasicToken.fromRequest(req);
        if (token == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing/invalid header");
        }

        // Find user.
        User user = userDao.findByUsername(token.username());
        if (user == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // Verify password.
        if (!user.verifyPassword(token.password())) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        return AuthToken.generate(user);
    }
}

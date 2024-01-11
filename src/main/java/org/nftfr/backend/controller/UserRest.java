package org.nftfr.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@RestController
public class UserRest {
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

    private record LoginParams(String username, String password) {}

    private LoginParams parseBasicAuth(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.contains("Basic ")) {
            String token = auth.substring("Basic ".length());
            String decodedToken = new String(Base64.getDecoder().decode(token));
            return new LoginParams(decodedToken.split(":")[0], decodedToken.split(":")[1]);
        }

        return null;
    }

    @PostMapping(value = "/user/register")
    public void register(@RequestBody RegisterParams params, HttpServletResponse res) {
        UserDao userDao = DBManager.getInstance().getUserDao();
        if (!userDao.register(params.asUser()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");

        res.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @PostMapping(value = "/user/login")
    public void login(HttpServletRequest req, HttpServletResponse res) {
        // Parse basic token.
        LoginParams params = parseBasicAuth(req);
        if (params == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing/invalid header");
        }

        // Find user.
        UserDao userDao = DBManager.getInstance().getUserDao();
        User user = userDao.findByUsername(params.username());
        if (user == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // Verify password.
        if (!user.verifyPassword(params.password())) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        // TODO: return auth token.
        System.out.println("Logged in as: " + params.username());
        res.setStatus(HttpStatus.NO_CONTENT.value());
    }
}

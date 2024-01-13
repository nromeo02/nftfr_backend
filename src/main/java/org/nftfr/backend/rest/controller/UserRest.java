package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.BasicToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserRest {
    private final UserDao userDao = DBManager.getInstance().getUserDao();

    public record RegisterParams(String username, String name, String surname, String password) {
        public User asUser() {
            User user = new User();
            user.setUsername(username);
            user.setName(name);
            user.setSurname(surname);
            user.setEncryptedPw(User.encryptPassword(password));
            return user;
        }
    }

    public record UpdateParams(String name, String surname, String password, int rank) {
        public User asUser(AuthToken authToken) {
            User user = new User();
            user.setUsername(authToken.username());
            user.setName(name);
            user.setSurname(surname);
            user.setEncryptedPw(User.encryptPassword(password));
            user.setRank(rank);
            return user;
        }
    }

    public record DeleteParams(String username) {}

    @PostMapping(value = "/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@RequestBody RegisterParams params) {
        if (!userDao.register(params.asUser()))
            throw new ClientErrorException(HttpStatus.CONFLICT, "Username is already taken");
    }

    @PostMapping(value = "/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthToken login(HttpServletRequest req, HttpServletResponse res) {
        // Get basic token.
        BasicToken token = BasicToken.fromRequest(req);
        if (token == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "Missing/invalid header");
        }

        // Find user.
        User user = userDao.findByUsername(token.username());
        if (user == null) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // Verify password.
        if (!user.verifyPassword(token.password())) {
            res.setHeader("WWW-Authenticate", "Basic");
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        return AuthToken.generate(user);
    }

    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody UpdateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        // TODO: rank update? admin update?
        userDao.update(params.asUser(authToken));
    }

    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestBody DeleteParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        // Only admins and the user itself can delete a user.
        if (!authToken.username().equals(params.username()) && !authToken.admin())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Admin privileges required");

        userDao.delete(params.username());
    }
}

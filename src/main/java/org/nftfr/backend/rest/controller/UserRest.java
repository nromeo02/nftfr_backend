package org.nftfr.backend.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.BasicToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
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

    public record UpdateParams(String name, String surname, String password) {}

    @PutMapping(value = "/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@RequestBody RegisterParams params) {
        if (!userDao.register(params.asUser()))
            throw new ClientErrorException(HttpStatus.CONFLICT, "Username is already taken");
    }

    @GetMapping(value = "/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthToken login(HttpServletRequest req) {
        // Get basic token.
        BasicToken token = BasicToken.fromRequest(req);
        if (token == null)
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid token");

        // Find user.
        User user = userDao.findByUsername(token.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "User not found");

        // Verify password.
        if (!user.verifyPassword(token.password()))
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "Authentication failed");

        return AuthToken.generate(user);
    }

    @PutMapping(value = "/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody UpdateParams params, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = userDao.findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The user does not exist");

        user.setName(params.name());
        user.setSurname(params.surname());
        user.setEncryptedPw(User.encryptPassword(params.password()));
        userDao.update(user);
    }

    private void delete(String username) {
        User user = userDao.findByUsername(username);
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The user does not exist");

        userDao.delete(username);
    }

    @DeleteMapping(value = "/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSelf(HttpServletRequest req) { delete(AuthToken.fromRequest(req).username()); }

    @DeleteMapping(value = "/delete/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        if (!authToken.admin())
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

       delete(username);
    }
    @GetMapping("/get/{username}")
    @ResponseStatus(HttpStatus.OK)
    public User get(@PathVariable String username) {
        User user = userDao.findByUsername(username);
        if(user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "The user does not exist");

        return user;
    }
}

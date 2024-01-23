package org.nftfr.backend.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.UserDao;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.utility.AuthToken;
import org.nftfr.backend.utility.BasicToken;
import org.nftfr.backend.utility.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/user")
public class UserRest {
    private final UserDao userDao = DBManager.getInstance().getUserDao();

    public record RegisterBody(String username, String name, String surname, String password) {
        public User asUser() {
            User user = new User();
            user.setUsername(username);
            user.setName(name);
            user.setSurname(surname);
            user.setEncryptedPw(User.encryptPassword(password));
            return user;
        }
    }

    public record UpdateBody(String name, String surname, String password) {}

    @PutMapping(value = "/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterBody bodyParams) {
        if (!userDao.register(bodyParams.asUser()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Username is already taken");
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
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Verify password.
        if (!user.verifyPassword(token.password()))
            throw new ClientErrorException(HttpStatus.UNAUTHORIZED, "Authentication failed");

        return AuthToken.generate(user);
    }

    @PutMapping(value = "/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody UpdateBody bodyParams, HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = userDao.findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        user.setName(bodyParams.name());
        user.setSurname(bodyParams.surname());
        user.setEncryptedPw(User.encryptPassword(bodyParams.password()));
        userDao.update(user);
    }

    @DeleteMapping(value = "/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(HttpServletRequest req) {
        AuthToken authToken = AuthToken.fromRequest(req);
        User user = userDao.findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        // Remove all payment methods and nfts for this user, then remove the user.
        // TODO: handle NFT removal (it will not work for now if the user owns any NFT).
        DBManager.getInstance().beginTransaction();
        DBManager.getInstance().getPaymentMethodDao().deleteByUsername(authToken.username());
        userDao.delete(authToken.username());
        DBManager.getInstance().endTransaction();
    }

    @GetMapping("/get/{username}")
    @ResponseStatus(HttpStatus.OK)
    public User get(@PathVariable String username) {
        User user = userDao.findByUsername(username);
        if(user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        return user;
    }
}

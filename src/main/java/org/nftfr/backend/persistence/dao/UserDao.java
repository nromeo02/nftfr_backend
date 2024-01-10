package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.User;

public interface UserDao {
    // Returns true if the user has been successfully registered, or false if a user with the same username exists.
    boolean register(User user);
    void update(User user);
    void delete(User user);
    User findByUsername(String username);
}

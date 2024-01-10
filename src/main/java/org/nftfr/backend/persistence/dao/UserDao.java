package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.User;

public interface UserDao {
    void register(User user);
    void update(User user);
    void delete(User user);
    User findByUsername(String username);
}

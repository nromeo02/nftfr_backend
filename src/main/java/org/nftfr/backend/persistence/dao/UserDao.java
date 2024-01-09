package org.nftfr.backend.persistence.dao;

import org.nftfr.backend.persistence.model.User;

public interface UserDao {
    void registerUser(User user);
    void updateUser(User user);
    void deleteUser(User user);
    void findUserByUsername(String username);
}

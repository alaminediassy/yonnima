package org.nema.service;


import org.nema.entities.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserService {
    AppUser registerAppUser(AppUser appUser);
    void saveUser(AppUser user);
    Optional<AppUser> getUserById(Long userId);
    List<AppUser> getAllUsers();
    AppUser updateUser(Long userId, AppUser updatedUser);
    void deleteUser(Long userId);
}

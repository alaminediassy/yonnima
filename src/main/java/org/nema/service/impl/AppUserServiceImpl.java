package org.nema.service.impl;

import jakarta.transaction.Transactional;
import org.nema.entities.AppUser;
import org.nema.entities.Role;
import org.nema.entities.VerificationToken;
import org.nema.repository.AppUserRepository;
import org.nema.repository.RoleRepository;
import org.nema.repository.VerificationTokenRepository;
import org.nema.service.AppUserService;
import org.nema.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    public AppUserServiceImpl(
            final AppUserRepository repository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService, VerificationTokenRepository verificationTokenRepository)
    {
        this.appUserRepository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }


    @Override
    @Transactional
    public AppUser registerAppUser(AppUser appUser) {
        // Validation de l'email
        if (!appUser.getEmail().contains("@") || !appUser.getEmail().contains(".")) {
            throw new RuntimeException("Invalid email format");
        }

        // Vérification des doublons d'email et de téléphone
        if (appUserRepository.findByEmail(appUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (appUserRepository.findByPhone(appUser.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exists");
        }

        // Hash du mot de passe
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));

        // Gestion du rôle utilisateur
        Role role = appUser.getRole();
        if (role == null || role.getName() == null || role.getName().isEmpty()) {
            // Si le rôle n'est pas défini ou a un nom vide, utiliser un rôle par défaut
            role = roleRepository.findByName("ROLE_STANDARD_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_STANDARD_USER not found"));
        } else {
            // Assurer que le rôle a le préfixe 'ROLE_'
            String roleName = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();

            // Rechercher le rôle ou le créer si absent, en assurant que le nom n’est pas nul
            final String finalRoleName = roleName != null ? roleName : "ROLE_STANDARD_USER";
            final String finalDescription = role.getDescription() != null ? role.getDescription() : "";

            role = roleRepository.findByName(finalRoleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(finalRoleName);
                        newRole.setDescription(finalDescription);
                        return roleRepository.save(newRole);
                    });
        }

        // Associer le rôle à l'utilisateur
        appUser.setRole(role);

        // Enregistrement de l'utilisateur
        AppUser createdUser = appUserRepository.save(appUser);

        // Création et envoi du token de vérification
        VerificationToken token = new VerificationToken(createdUser);
        verificationTokenRepository.save(token);
        emailService.sendVerificationEmail(createdUser.getEmail(), token.getToken());

        return createdUser;
    }


    @Override
    public void saveUser(AppUser user) {
        appUserRepository.save(user);
    }

    @Override
    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public List<AppUser> getAllUsers() {
        return (List<AppUser>) appUserRepository.findAll();
    }

    public Optional<AppUser> getAllUsers(Long userId) {
        return appUserRepository.findById(userId);
    }

    @Override
    public AppUser updateUser(Long userId, AppUser updatedUser) {
        AppUser existingUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFirstname(updatedUser.getFirstname());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setStatus(updatedUser.getStatus());
        existingUser.setRole(updatedUser.getRole());

        return appUserRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        appUserRepository.deleteById(userId);
    }

}

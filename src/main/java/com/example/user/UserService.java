// UserService.java
package com.example.user;

import java.util.regex.Pattern;

public class UserService {
    private static final Pattern EMAIL_RX =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final UserRepository repo;
    private final PasswordHasher hasher;

    public UserService(UserRepository repo, PasswordHasher hasher) {
        this.repo = repo; this.hasher = hasher;
    }

    public User register(RegisterRequest req) {
        if (req == null) throw new RegistrationException("Request is required");
        if (req.name() == null || req.name().isBlank())
            throw new RegistrationException("Name is required");
        if (req.email() == null || !EMAIL_RX.matcher(req.email()).matches())
            throw new RegistrationException("Invalid email");
        validatePassword(req.password());

        if (repo.existsByEmail(req.email().toLowerCase()))
            throw new RegistrationException("Email already registered");

        String hash = hasher.hash(req.password());
        User toSave = new User(null, req.name().trim(), req.email().toLowerCase(), hash);
        return repo.save(toSave);
    }

    private void validatePassword(String pwd) {
        if (pwd == null || pwd.length() < 8) throw new RegistrationException("Weak password");
        boolean hasUpper=false, hasLower=false, hasDigit=false;
        for (char c : pwd.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper=true;
            else if (Character.isLowerCase(c)) hasLower=true;
            else if (Character.isDigit(c)) hasDigit=true;
        }
        if (!(hasUpper && hasLower && hasDigit))
            throw new RegistrationException("Weak password");
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank())
            throw new RegistrationException("Email is required");
        if (password == null || password.isBlank())
            throw new RegistrationException("Password is required");
        User user = repo.findByEmail(email.toLowerCase());
        if (user == null)
            throw new RegistrationException("Invalid credentials");
        if (!hasher.hash(password).equals(user.passwordHash()))
            throw new RegistrationException("Invalid credentials");
        return user;
    }
}
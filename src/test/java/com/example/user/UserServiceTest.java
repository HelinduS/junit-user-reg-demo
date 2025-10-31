package com.example.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("User registration service")
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock PasswordHasher hasher;

    @InjectMocks UserService service;

    @BeforeEach
    void setup() {
        
        lenient().when(repo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(hasher.hash(anyString())).then(inv -> "HASH(" + inv.getArgument(0) + ")");
        lenient().when(repo.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(100L, u.name(), u.email(), u.passwordHash());
        });
    }

    @Test
    @DisplayName("Demo failing test")
    void demoFail() {
        assertEquals(1, 2, "This test intentionally fails to show JUnit's failure report");
    }

    @Test
    @DisplayName("Happy path: valid request creates user with hashed password")
    void registersSuccessfully() {
        RegisterRequest req = new RegisterRequest("Alice", "Alice@Example.com", "Abcdef12");
        User saved = service.register(req);
        assertAll(
            () -> assertNotNull(saved),
            () -> assertEquals(100L, saved.id()),
            () -> assertEquals("alice@example.com", saved.email()),
            () -> assertTrue(saved.passwordHash().startsWith("HASH("))
        );
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(repo).save(cap.capture());
        assertEquals("Alice", cap.getValue().name());
    }

    @Test
    @DisplayName("Duplicate email is rejected")
    void duplicateEmail() {
        when(repo.existsByEmail("bob@example.com")).thenReturn(true);
        var ex = assertThrows(RegistrationException.class, () ->
            service.register(new RegisterRequest("Bob", "bob@example.com", "Abcdef12")));
        assertEquals("Email already registered", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @ParameterizedTest(name = "{index} => email=''{0}''")
    @ValueSource(strings = {"bad@", "@bad.com", "a@b", "abc"})
    @DisplayName("Invalid email formats are rejected")
    void invalidEmails(String email) {
        var ex = assertThrows(RegistrationException.class, () -> service.register(new RegisterRequest("A", email, "Abcdef12")));
        assertEquals("Invalid email", ex.getMessage());
    }

    @ParameterizedTest(name = "{index} => pwd=''{0}''")
    @ValueSource(strings = {"short7", "alllowercase1", "ALLUPPER1", "NoDigitsHere"})
    @DisplayName("Weak passwords are rejected")
    void weakPasswords(String pwd) {
        var ex = assertThrows(RegistrationException.class, () -> service.register(new RegisterRequest("A", "ok@example.com", pwd)));
        assertEquals("Weak password", ex.getMessage());
    }

    @Test
    @DisplayName("Null request is rejected")
    void nullRequest() {
    assertThrows(RegistrationException.class, () -> service.register(null));
    }

    @Test
    @DisplayName("Login succeeds with correct credentials")
    void loginSuccess() {
        String email = "alice@example.com";
        String password = "Abcdef12";
        User user = new User(101L, "Alice", email, hasher.hash(password));
        when(repo.findByEmail(email)).thenReturn(user);
        User loggedIn = service.login(email, password);
        assertEquals(user, loggedIn);
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void loginWrongPassword() {
        String email = "alice@example.com";
        String password = "Abcdef12";
        User user = new User(101L, "Alice", email, hasher.hash(password));
        when(repo.findByEmail(email)).thenReturn(user);
        assertThrows(RegistrationException.class, () -> service.login(email, "wrongpass"));
    }

    @Test
    @DisplayName("Login fails with unknown email")
    void loginUnknownEmail() {
        when(repo.findByEmail(anyString())).thenReturn(null);
        assertThrows(RegistrationException.class, () -> service.login("notfound@example.com", "Abcdef12"));
    }
}
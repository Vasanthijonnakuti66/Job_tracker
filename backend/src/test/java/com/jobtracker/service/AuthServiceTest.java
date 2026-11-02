package com.jobtracker.service;

import com.jobtracker.dto.AuthResponse;
import com.jobtracker.dto.LoginRequest;
import com.jobtracker.dto.RegisterRequest;
import com.jobtracker.entity.User;
import com.jobtracker.exception.DuplicateResourceException;
import com.jobtracker.exception.UnauthorizedException;
import com.jobtracker.repository.UserRepository;
import com.jobtracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(60_000L);

        AuthResponse response = service.register(
                new RegisterRequest(" Person@Example.com ", "password123", "  Person Name  "));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("person@example.com", userCaptor.getValue().getEmail());
        assertEquals("bcrypt-hash", userCaptor.getValue().getPassword());
        assertEquals("Person Name", userCaptor.getValue().getFullName());
        assertEquals("jwt-token", response.accessToken());
        assertEquals(3L, response.user().id());
    }

    @Test
    void registerRejectsDuplicateEmailWithoutSaving() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.register(
                new RegisterRequest("Person@Example.com", "password123", "Person")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUsesSameErrorForUnknownEmailAndWrongPassword() {
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.empty());
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("Person@Example.com", "wrong")));
        assertEquals("Invalid email or password", exception.getMessage());

        User user = new User("person@example.com", "bcrypt-hash", "Person");
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "bcrypt-hash")).thenReturn(false);
        UnauthorizedException wrongPassword = assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("Person@Example.com", "wrong")));
        assertEquals(exception.getMessage(), wrongPassword.getMessage());
    }
}

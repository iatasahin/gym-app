package dev.ilkersahin.java.spring.gym.security;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthContextHolderTest {
    @Mock private AuthContext authContext;
    @InjectMocks private AuthContextHolder authContextHolder;

    @Test
    @Order(101)
    void getAuthenticatedUsername() {
        var expectedUsername = "testUser";
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getUsername()).thenReturn(expectedUsername);

        var actualUsername = AuthContextHolder.getAuthenticatedUsername();

        assertTrue(actualUsername.isPresent());
        assertEquals(expectedUsername, actualUsername.get());
    }

    @Test
    @Order(102)
    void getAuthenticatedUsername_whenUnauthenticated_returnsEmpty() {
        when(authContext.isAuthenticated()).thenReturn(false);

        Optional<String> actual = AuthContextHolder.getAuthenticatedUsername();

        assertTrue(actual.isEmpty());
    }

    @Test
    @Order(103)
    void getAuthenticatedUsername_whenAuthenticatedButNullUsername_returnsEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getUsername()).thenReturn(null);

        Optional<String> actual = AuthContextHolder.getAuthenticatedUsername();

        assertTrue(actual.isEmpty());
    }

    @Test
    @Order(104)
    void getAuthenticatedRole_whenUnauthenticated_returnsEmpty() {
        when(authContext.isAuthenticated()).thenReturn(false);

        Optional<Role> actual = AuthContextHolder.getAuthenticatedRole();

        assertTrue(actual.isEmpty());
    }

    @Test
    @Order(105)
    void getAuthenticatedRole_whenAuthenticated_returnsRole() {
        Role expectedRole = Role.TRAINEE;
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getRole()).thenReturn(expectedRole);

        Optional<Role> actual = AuthContextHolder.getAuthenticatedRole();

        assertTrue(actual.isPresent());
        assertEquals(expectedRole, actual.get());
    }

    @Test
    @Order(106)
    void getAuthenticatedRole_whenAuthenticatedButNullRole_returnsEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getRole()).thenReturn(null);

        Optional<Role> actual = AuthContextHolder.getAuthenticatedRole();

        assertTrue(actual.isEmpty());
    }

    @Test
    @Order(107)
    void whenAuthContextIsNull_returnsEmptyForUsernameAndRole() throws Exception {
        Field f = AuthContextHolder.class.getDeclaredField("authContext");
        f.setAccessible(true);
        Object original = f.get(null);
        try {
            f.set(null, null);
            assertTrue(AuthContextHolder.getAuthenticatedUsername().isEmpty());
            assertTrue(AuthContextHolder.getAuthenticatedRole().isEmpty());
        } finally {
            f.set(null, original); // restore
        }
    }
}

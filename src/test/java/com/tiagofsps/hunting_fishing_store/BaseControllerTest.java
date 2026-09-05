package com.tiagofsps.hunting_fishing_store;

import com.tiagofsps.hunting_fishing_store.config.security.AuthorizationUtil;
import com.tiagofsps.hunting_fishing_store.user.dto.response.UserResponse;
import com.tiagofsps.hunting_fishing_store.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public abstract class BaseControllerTest extends BaseIntegrationTest {

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected AuthorizationUtil authorizationUtil;

    @MockitoBean
    protected UserDetailsService userDetailsService;

    @BeforeEach
    void baseControllerSetup() {
        // Default setup for authorization ownership checks
        UserResponse defaultUser = new UserResponse(1L, "test@example.com", "User", "Test", null, null, null, true, List.of());
        when(userService.findById(anyLong())).thenReturn(defaultUser);
        
        when(authorizationUtil.getAuthenticatedUser()).thenReturn("test@example.com");
        when(authorizationUtil.isAdmin()).thenReturn(false);
    }
}

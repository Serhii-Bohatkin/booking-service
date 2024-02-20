package bookingservice.controller;

import bookingservice.dto.user.UserResponseDto;
import bookingservice.dto.user.UserUpdateDto;
import bookingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users management", description = "Endpoints for managing users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user role", description = "Update user role by email and role id")
    @PutMapping("/{email}/role/{roleId}")
    public UserResponseDto updateRolesByEmail(@PathVariable String email,
                                              @PathVariable Long roleId) {
        return userService.addRole(email, roleId);
    }

    @GetMapping("/me")
    @Operation(summary = "Get user information",
            description = "Get information about the current user")
    public UserResponseDto getCurrentUserInfo() {
        return userService.getCurrentUserInfo();
    }

    @PutMapping("/me")
    @Operation(summary = "Update user information",
            description = "Update information of the current user")
    public UserResponseDto updateUser(@RequestBody @Valid UserUpdateDto updateDto) {
        return userService.update(updateDto);
    }
}

package bookingservice.controller;

import bookingservice.dto.user.UserLoginRequestDto;
import bookingservice.dto.user.UserLoginResponseDto;
import bookingservice.dto.user.UserRegistrationRequestDto;
import bookingservice.dto.user.UserResponseDto;
import bookingservice.exception.RegistrationException;
import bookingservice.security.AuthenticationService;
import bookingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication management", description = "Endpoints for managing authentications")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Registration", description = "Registration of new users")
    @PostMapping("/registration")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @Operation(summary = "Authentication",
            description = "Authentication users by email and password")
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}

package bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import bookingservice.dto.user.UserRegistrationRequestDto;
import bookingservice.dto.user.UserResponseDto;
import bookingservice.dto.user.UserUpdateDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.exception.RegistrationException;
import bookingservice.mapper.UserMapper;
import bookingservice.model.Role;
import bookingservice.model.User;
import bookingservice.repository.RoleRepository;
import bookingservice.repository.UserRepository;
import bookingservice.service.impl.UserServiceImpl;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    public static final String USER_EMAIL = "admin@gmail.com";
    public static final String USER_PASSWORD = "@g_sJ'#_$ks%1Nq";
    public static final long USER_ID = 1L;
    public static final long ROLE_ID = 2L;
    public static final String INVALID_EMAIL = "Not email";
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void register_ValidDto_ShouldReturnUserResponseDto() throws RegistrationException {
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto();
        User user = createUser();
        UserResponseDto expected = createUserResponseDto();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.password())).thenReturn(USER_PASSWORD);
        when(roleRepository.findByName(Role.RoleName.USER))
                .thenReturn(Optional.of(createRoleUser()));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void register_UserAlreadyExistInDb_Exception() throws RegistrationException {
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto();

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(createUser()));
        assertThatExceptionOfType(RegistrationException.class)
                .isThrownBy(() -> userService.register(requestDto))
                .withMessage("Registration cannot be completed because"
                        + " a user with the same email address already exists");
    }

    @Test
    public void addRole_ExistingEmailAndId_ShouldReturnUserResponseDto() {
        User user = createUser();
        UserResponseDto expected = createUserResponseDto();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(createRoleAdmin()));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.addRole(USER_EMAIL, ROLE_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void addRole_NonExistingEmail_ShouldThrowException() {
        when(userRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> userService.addRole(INVALID_EMAIL, ROLE_ID))
                .withMessage("User with email " + INVALID_EMAIL + "not found");
    }

    @Test
    public void addRole_NonExistingRoleId_ShouldThrowException() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUser()));
        when(roleRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> userService.addRole(USER_EMAIL, Long.MAX_VALUE))
                .withMessage("Can't find a role with id " + Long.MAX_VALUE);
    }

    @Test
    public void getCurrentUserInfo_ShouldReturnUserResponseDto() {
        User user = createUser();
        UserResponseDto expected = createUserResponseDto();

        when(userMapper.toDto(user)).thenReturn(expected);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        UserResponseDto actual = userService.getCurrentUserInfo();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void update_ValidUserUpdateDto_ShouldReturnUserResponseDto() {
        User user = createUser();
        UserResponseDto expected = createSecondUserResponseDto();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userMapper.toDto(user)).thenReturn(expected);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.save(user)).thenReturn(user);

        UserResponseDto actual = userService.update(new UserUpdateDto("Adam", "Smith"));
        assertThat(actual).isEqualTo(expected);
    }

    private UserResponseDto createUserResponseDto() {
        return new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                "Jhon",
                "Doe"
        );
    }

    private UserResponseDto createSecondUserResponseDto() {
        return new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                "Adam",
                "Smith"
        );
    }

    private User createUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(createRoleUser());
        roles.add(createRoleAdmin());

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setFirstName("Jhon");
        user.setLastName("Doe");
        user.setPassword(USER_PASSWORD);
        user.setRoles(roles);
        return user;
    }

    private Role createRoleUser() {
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName(Role.RoleName.USER);
        return roleUser;
    }

    private Role createRoleAdmin() {
        Role roleAdmin = new Role();
        roleAdmin.setId(2L);
        roleAdmin.setName(Role.RoleName.ADMIN);
        return roleAdmin;
    }

    private UserRegistrationRequestDto createUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                USER_EMAIL,
                USER_PASSWORD,
                USER_PASSWORD,
                "Jhon",
                "Doe"
        );
    }
}

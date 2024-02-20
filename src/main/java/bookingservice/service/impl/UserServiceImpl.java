package bookingservice.service.impl;

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
import bookingservice.service.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Value("${admin.email}")
    private String adminEmail;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("Registration cannot be completed because"
                    + " a user with the same email address already exists");
        }
        User model = userMapper.toModel(requestDto);
        User user = new User();
        model.setPassword(passwordEncoder.encode(requestDto.password()));
        model.setRoles(assignDefaultRoles(requestDto.email()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto addRole(String email, Long roleId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email " + email + "not found"));
        user.getRoles().add(getRoleById(roleId));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getCurrentUserInfo() {
        return userMapper.toDto(getCurrentUser());
    }

    @Override
    public UserResponseDto update(UserUpdateDto updateDto) {
        User user = getCurrentUser();
        user.setFirstName(updateDto.firstName());
        user.setLastName(updateDto.lastName());
        return userMapper.toDto(userRepository.save(user));
    }

    private Role getRoleById(Long id) {
        return roleRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find a role with id " + id));
    }

    private Set<Role> assignDefaultRoles(String email) {
        Set<Role> roles = new HashSet<>();
        Role defaultRoleForNewUser = roleRepository.findByName(Role.RoleName.USER).orElseThrow(
                () -> new EntityNotFoundException("Can't find default role"));
        roles.add(defaultRoleForNewUser);
        assignAdminRoleByEmail(email, roles);
        return roles;
    }

    private void assignAdminRoleByEmail(String email, Set<Role> roles) {
        if (email.equals(adminEmail)) {
            roles.add(roleRepository.findByName(Role.RoleName.ADMIN).orElseThrow(
                    () -> new EntityNotFoundException("Can't find role admin")));
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}

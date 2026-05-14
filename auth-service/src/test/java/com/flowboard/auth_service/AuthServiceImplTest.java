package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.impl.SignupRequestMapper;
import com.flowboard.auth_service.Mapper.impl.UserResponseMapper;
import com.flowboard.auth_service.config.AdminBootstrapConfig;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.JwtService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.service.UserVerificationService;
import com.flowboard.auth_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SignupRequestMapper signupRequestMapper;
    @Mock
    private UserResponseMapper userResponseMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserVerificationService userVerificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;
    @Mock
    private UserOtpService userOtpService;
    @Mock
    private UserOtpRepository userOtpRepository;
    @Mock
    private UserVerificationRepository userVerificationRepository;
    @Mock
    private AdminBootstrapConfig adminConfig;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_withValidData_returnsUserDto() {
        SignupDto signupDto = signupDto("tarun@gmail.com", "ABC123");
        User user = user("tarun@gmail.com", ROLE.USER, false, 1);
        user.setPassword("123456");
        User savedUser = user("tarun@gmail.com", ROLE.USER, true, 1);
        savedUser.setPassword("encodedPassword");
        UserDto userDto = new UserDto();
        userDto.setUserId(1);
        userDto.setFullName("Tarun");
        userDto.setEmail("tarun@gmail.com");

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(userRepository.findByEmail("tarun@gmail.com")).thenReturn(Optional.empty());
        doNothing().when(userOtpService).validateSignupOtp("tarun@gmail.com", "ABC123");
        when(signupRequestMapper.mapTo(signupDto)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userResponseMapper.mapTo(savedUser)).thenReturn(userDto);

        UserDto result = authService.register(signupDto);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("tarun@gmail.com", result.getEmail());
    }

    @Test
    void register_withReservedAdminEmail_throwsException() {
        SignupDto signupDto = signupDto("admin@flowboard.com", "ABC123");
        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.register(signupDto));

        assertEquals("This email is reserved for admin login only", exception.getMessage());
    }

    @Test
    void register_withExistingActiveUser_throwsException() {
        SignupDto signupDto = signupDto("john@gmail.com", "ABC123");
        User existingUser = user("john@gmail.com", ROLE.USER, true, 9);

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));

        assertThrows(UserNotFoundException.class, () -> authService.register(signupDto));
    }

    @Test
    void register_withExistingInactiveUser_replacesUser() {
        SignupDto signupDto = signupDto("john@gmail.com", "OTP123");
        User existingUser = user("john@gmail.com", ROLE.USER, false, 9);
        existingUser.setPassword("old");
        User mappedUser = user("john@gmail.com", ROLE.USER, false, null);
        mappedUser.setPassword("newPass");
        User savedUser = user("john@gmail.com", ROLE.USER, true, 10);
        UserDto userDto = new UserDto();
        userDto.setUserId(10);

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));
        doNothing().when(userOtpService).validateSignupOtp("john@gmail.com", "OTP123");
        when(signupRequestMapper.mapTo(signupDto)).thenReturn(mappedUser);
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userResponseMapper.mapTo(savedUser)).thenReturn(userDto);

        UserDto result = authService.register(signupDto);

        assertEquals(10, result.getUserId());
        verify(userRepository).delete(existingUser);
        verify(userVerificationRepository).deleteByUserId(9);
    }

    @Test
    void register_withoutOtp_throwsException() {
        SignupDto signupDto = signupDto("john@gmail.com", null);

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.empty());

        OtpException exception = assertThrows(OtpException.class, () -> authService.register(signupDto));

        assertEquals("Signup OTP is required", exception.getMessage());
    }

    @Test
    void registerAdmin_alwaysThrows() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerAdmin(signupDto("admin@flowboard.com", "ABC123")));

        assertEquals("Admin registration is disabled. Configure admin credentials in application.yml.", exception.getMessage());
    }

    @Test
    void login_withConfiguredAdmin_returnsAdminToken() {
        LoginDto loginDto = new LoginDto("admin@flowboard.com", "Password@1");

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(adminConfig.getPassword()).thenReturn("encoded-admin");
        when(passwordEncoder.matches("Password@1", "encoded-admin")).thenReturn(true);
        when(jwtService.generateToken("admin@flowboard.com", "ADMIN", -1)).thenReturn("admin-token");

        String result = authService.login(loginDto);

        assertEquals("admin-token", result);
    }

    @Test
    void login_withConfiguredAdminMissingPassword_throwsException() {
        LoginDto loginDto = new LoginDto("admin@flowboard.com", "Password@1");

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(adminConfig.getPassword()).thenReturn(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginDto));

        assertEquals("Admin password hash is missing in application.yml", exception.getMessage());
    }

    @Test
    void login_withValidData_returnsToken() {
        LoginDto loginDto = new LoginDto("john@gmail.com", "Password@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("john@gmail.com", "Password@1"));
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("john@gmail.com", "USER", 1)).thenReturn("jwt-token");

        String result = authService.login(loginDto);

        assertEquals("jwt-token", result);
    }

    @Test
    void login_withWrongEmail_throwsException() {
        LoginDto loginDto = new LoginDto("wrong@gmail.com", "Password@1");

        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("wrong@gmail.com", "Password@1"));
        when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginDto));
    }

    @Test
    void verify_withValidToken_updatesUser() {
        UserVerification verification = UserVerification.builder().userId(1).token("abc").build();
        User user = user("john@gmail.com", ROLE.USER, false, 1);

        when(userVerificationService.findByToken("abc")).thenReturn(verification);
        when(userService.findById(1)).thenReturn(user);
        doNothing().when(userVerificationService).deleteByUserId(1);

        authService.verify("abc");

        verify(userRepository).save(user);
    }

    @Test
    void sendOtp_withValidEmail_callsService() {
        doNothing().when(userOtpService).sendOtp("john@gmail.com");

        authService.sendOtp("john@gmail.com");

        verify(userOtpService).sendOtp("john@gmail.com");
    }

    @Test
    void sendSignupOtp_withAdminEmail_throwsException() {
        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.sendSignupOtp("admin@flowboard.com"));

        assertEquals("Admin email cannot be used for user signup", exception.getMessage());
    }

    @Test
    void sendSignupOtp_withRegularEmail_callsService() {
        when(adminConfig.getEmail()).thenReturn("admin@flowboard.com");

        authService.sendSignupOtp("user@flowboard.com");

        verify(userOtpService).sendSignupOtp("user@flowboard.com");
    }

    @Test
    void changePassword_withValidOtp_updatesPassword() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "123456", "NewPass@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);
        UserOtp otp = otp(1, "123456", LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("NewPass@1")).thenReturn("encoded");

        authService.changePassword(dto);

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withMissingUser_throwsException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "123456", "NewPass@1");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withMissingOtp_throwsException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "123456", "NewPass@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withWrongOtp_throwsException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "999999", "NewPass@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);
        UserOtp otp = otp(1, "123456", LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withExpiredOtp_throwsException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "123456", "NewPass@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);
        UserOtp otp = otp(1, "123456", LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withOtpOwnershipMismatch_throwsException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "123456", "NewPass@1");
        User user = user("john@gmail.com", ROLE.USER, true, 1);
        UserOtp otp = otp(2, "123456", LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    private SignupDto signupDto(String email, String otp) {
        SignupDto signupDto = new SignupDto();
        signupDto.setFullName("Tarun");
        signupDto.setEmail(email);
        signupDto.setPassword("123456");
        signupDto.setOtp(otp);
        return signupDto;
    }

    private User user(String email, ROLE role, boolean active, Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName("Tarun");
        user.setEmail(email);
        user.setPassword("Password@1");
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private UserOtp otp(Integer userId, String otp, LocalDateTime time) {
        UserOtp userOtp = new UserOtp();
        userOtp.setUserId(userId);
        userOtp.setOtp(otp);
        userOtp.setLastOtpDateTime(time);
        return userOtp;
    }
}

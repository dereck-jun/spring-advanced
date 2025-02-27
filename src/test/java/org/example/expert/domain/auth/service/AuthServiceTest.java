package org.example.expert.domain.auth.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.util.JwtUtil;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    public void 회원가입이_정상적으로_성공한다() {
        // given
        String email = "a@a.com";
        SignupRequest request = new SignupRequest(email, "password", "user");
        String encodedPassword = "encodedPassword";
        User mockUser = new User(email, encodedPassword, UserRole.USER);
        String mockToken = "mockToken";

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(mockUser);
        given(jwtUtil.createToken(mockUser.getId(), mockUser.getEmail(), mockUser.getUserRole())).willReturn(mockToken);

        // when
        SignupResponse result = authService.signup(request);

        // then
        assertNotNull(result);
        assertEquals(mockToken, result.getBearerToken());
    }

    @Test
    public void 회원가입_시_이메일_중복으로_인해_실패한다() {
        // given
        String email = "a@a.com";
        SignupRequest request = new SignupRequest(email, "password", "user");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(request));

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    public void 로그인에_성공한다() {
        // given
        String email = "a@a.com";
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        SigninRequest request = new SigninRequest(email, rawPassword);
        User mockUser = new User(email, encodedPassword, UserRole.USER);
        String mockToken = "mockToken";

        given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
        given(jwtUtil.createToken(mockUser.getId(), mockUser.getEmail(), mockUser.getUserRole())).willReturn(mockToken);

        // when
        SigninResponse result = authService.signin(request);

        // then
        assertNotNull(result);
        assertEquals(mockToken, result.getBearerToken());
    }

    @Test
    public void 로그인_시_email을_찾을_수_없어_예외가_발생한다() {
        // given
        String email = "a@a.com";
        String rawPassword = "rawPassword";
        SigninRequest request = new SigninRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signin(request));

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }


    @Test
    public void 로그인_시_비밀번호가_일치하지_않아서_예외가_발생한다() {
        // given
        String email = "a@a.com";
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        User mockUser = new User(email, encodedPassword, UserRole.USER);
        SigninRequest request = new SigninRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class, () -> authService.signin(request));

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}
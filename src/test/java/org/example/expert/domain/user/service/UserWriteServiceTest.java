package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserWriteServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserWriteService userWriteService;

    @Test
    public void 비밀번호_변경_성공() {
        // given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String prevEncodedPassword = "prevEncodedPassword";
        String encodedPassword = "encodedPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User mockUser = new User("a@a.com", prevEncodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(newPassword, mockUser.getPassword())).willReturn(false);
        given(passwordEncoder.matches(oldPassword, mockUser.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn(encodedPassword);

        // when
        userWriteService.changePassword(mockUser.getId(), request);

        // then
        then(repository).should(times(1)).findById(1L);
        then(passwordEncoder).should().matches(newPassword, prevEncodedPassword);
        then(passwordEncoder).should().matches(oldPassword, prevEncodedPassword);
        then(passwordEncoder).should().encode(newPassword);
        assertEquals(encodedPassword, mockUser.getPassword());
    }

    @Test
    public void 새_비밀번호와_기존_비밀번호의_중복으로_인한_실패() {
        // given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String prevEncodedPassword = "prevEncodedPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User mockUser = new User("a@a.com", prevEncodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(newPassword, mockUser.getPassword())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userWriteService.changePassword(mockUser.getId(), request));

        // then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호_불일치로_인한_실패() {
        // given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String prevEncodedPassword = "prevEncodedPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User mockUser = new User("a@a.com", prevEncodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches(newPassword, mockUser.getPassword())).willReturn(false);
        given(passwordEncoder.matches(oldPassword, prevEncodedPassword)).willReturn(false);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userWriteService.changePassword(mockUser.getId(), request));

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    public void 유저_탈퇴_성공() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User mockUser = new User("a@a.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        // when
        userWriteService.deleteUser(authUser.getId(), mockUser.getId());

        // then
        then(repository).should(times(1)).deleteById(1L);
    }

    @Test
    public void 로그인한_유저의_다른_유저_탈퇴_시도로_인한_실패() {
        // given
        AuthUser authUser = new AuthUser(2L, "ab@a.com", UserRole.USER);
        User mockUser = new User("a@a.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userWriteService.deleteUser(authUser.getId(), mockUser.getId()));

        // then
        then(repository).should(times(0)).deleteById(1L);
        assertEquals("해당 요청에 대한 권한이 없습니다.", exception.getMessage());
    }


}
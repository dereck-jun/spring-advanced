package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.UserResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserReadServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserReadService userReadService;

    @Test
    public void userId로_User_객체를_가져오는데_성공한다() {
        // given
        User mockUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(1L)).willReturn(Optional.of(mockUser));

        // when
        User user = userReadService.getUserOrThrow(mockUser.getId());

        // then
        assertNotNull(user);
        assertEquals(user.getId(), mockUser.getId());
    }

    @Test
    public void userId로_User_객체를_가져오는데_실패한다() {
        // given
        User mockUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(2L)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userReadService.getUserOrThrow(2L));

        // then
        assertEquals("유저를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void userId로_UserResponse_객체를_가져오는데_성공한다() {
        // given
        User mockUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(1L)).willReturn(Optional.of(mockUser));

        // when
        UserResponse response = userReadService.getUserResponse(mockUser.getId());

        // then
        assertNotNull(response);
        assertEquals(mockUser.getId(), response.getId());
    }

    @Test
    public void userId로_UserResponse_객체를_가져오는데_실패한다() {
        // given
        User mockUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(repository.findById(2L)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userReadService.getUserResponse(2L));

        // then
        assertEquals("유저를 찾을 수 없습니다.", exception.getMessage());
    }
}
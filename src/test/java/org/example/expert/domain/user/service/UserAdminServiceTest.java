package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserReadService userReadService;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    public void 관리자가_유저의_역할을_변경하는_데_성공한다() {
        // given
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");
        User mockUser = new User("b@b.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(userReadService.getUserOrThrow(mockUser.getId())).willReturn(mockUser);

        // when
        userAdminService.changeUserRole(mockUser.getId(), request);

        // then
        assertEquals(UserRole.ADMIN, mockUser.getUserRole());
        then(userReadService).should(times(1)).getUserOrThrow(1L);
    }

    @Test
    public void 관리자가_존재하지_않는_유저의_역할_변경_시도로_인해_실패한다() {
        // given
        given(userReadService.getUserOrThrow(1L)).willThrow(new InvalidRequestException("유저를 찾을 수 없습니다."));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userReadService.getUserOrThrow(1L));

        // then
        assertEquals("유저를 찾을 수 없습니다.", exception.getMessage());
    }

}
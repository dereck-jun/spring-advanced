package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoReadService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ManagerWriteServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserReadService userReadService;
    @Mock
    private TodoReadService todoReadService;
    @InjectMocks
    private ManagerWriteService managerWriteService;

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        given(todoReadService.getTodoOrThrow(todoId)).willReturn(todo);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerWriteService.saveManager(authUser, todoId, managerUserId)
        );

        assertEquals("일정을 만든 유저를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
        // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        given(todoReadService.getTodoOrThrow(todoId)).willReturn(todo);
        given(userReadService.getUserOrThrow(managerUserId)).willReturn(managerUser);
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerWriteService.saveManager(authUser, todoId, managerUserId);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
        // 테스트코드 샘플
    void todo의_유저와_로그인한_유저가_달라_실패한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);

        User todoUser = new User("c@c.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", todoUser);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        given(todoReadService.getTodoOrThrow(todoId)).willReturn(todo);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerWriteService.saveManager(authUser, todoId, managerUserId));

        // then
        assertEquals("담당자를 등록하려고 하는 유저와 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
        // 테스트코드 샘플
    void todo의_유저와_로그인한_유저가_같아서_담당자_등록에_실패한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        given(todoReadService.getTodoOrThrow(todoId)).willReturn(todo);
        given(userReadService.getUserOrThrow(managerUserId)).willReturn(managerUser);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerWriteService.saveManager(authUser, todoId, managerUserId));

        // then
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 매니저_삭제에_성공한다() {
        // given
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "contents", "weather", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", 1L);

        given(managerRepository.findById(1L)).willReturn(Optional.of(manager));
        given(todoReadService.getTodoOrThrow(1L)).willReturn(todo);
        given(userReadService.getUserOrThrow(1L)).willReturn(user);

        // when
        doNothing().when(managerRepository).delete(manager);
        managerWriteService.deleteManager(user.getId(), manager.getId());

        // then
        then(managerRepository).should(times(1)).delete(manager);
        then(managerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    public void 일정을_만든_유저를_찾지_못해_실패한다() {
        // given
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "contents", "weather", null);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", 1L);

        given(managerRepository.findById(1L)).willReturn(Optional.of(manager));
        given(todoReadService.getTodoOrThrow(1L)).willReturn(todo);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerWriteService.deleteManager(user.getId(), manager.getId()));

        // then
        assertEquals("일정을 만든 유저를 찾을 수 없습니다.", exception.getMessage());
        then(managerRepository).should(times(0)).delete(manager);
        then(managerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    public void 일정을_만든_유저와_로그인한_유저가_달라_실패한다() {
        // given
        AuthUser authUser = new AuthUser(2L, "b@b.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        User todoUser = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(todoUser, "id", 1L);

        Todo todo = new Todo("title", "contents", "weather", todoUser);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Manager manager = new Manager(todoUser, todo);
        ReflectionTestUtils.setField(manager, "id", 1L);

        given(managerRepository.findById(1L)).willReturn(Optional.of(manager));
        given(todoReadService.getTodoOrThrow(1L)).willReturn(todo);
        given(userReadService.getUserOrThrow(2L)).willReturn(user);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerWriteService.deleteManager(user.getId(), manager.getId()));

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        then(managerRepository).should(times(0)).delete(manager);
        then(managerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    public void 일정을_만든_유저와_일정에_등록된_유저가_달라_실패한다() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "weather", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Todo todo2 = new Todo("title", "contents", "weather", user);
        ReflectionTestUtils.setField(todo2, "id", 2L);

        long managerId = 1L;
        Manager manager = new Manager(user, todo2);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));
        given(todoReadService.getTodoOrThrow(anyLong())).willReturn(todo);
        given(userReadService.getUserOrThrow(userId)).willReturn(user);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerWriteService.deleteManager(userId, todoId));

        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

}

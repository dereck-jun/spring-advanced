package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TodoWriteServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoWriteService todoWriteService;

    @Test
    public void 일정_저장에_성공한다() {
        // given
        String title = "title";
        String contents = "contents";
        String weather = weatherClient.getTodayWeather();
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest request = new TodoSaveRequest(title, contents);
        Todo todo = new Todo(title, contents, weather, user);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoSaveResponse response = todoWriteService.saveTodo(authUser, request);

        // then
        assertNotNull(response);
        assertEquals(todo.getId(), response.getId());
    }

    @Test
    public void 일정_수정에_성공한다() {
        // given
        String title = "title";
        String contents = "contents";
        String weather = weatherClient.getTodayWeather();
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoWriteService.updateTodo(authUser, todo.getId(), title, contents);

        // then
        assertNotNull(response);
        assertEquals(todo.getId(), response.getId());
    }

    @Test
    public void 권한이_없어서_일정_수정에_실패한다() {
        // given
        String title = "title";
        String contents = "contents";
        String weather = weatherClient.getTodayWeather();
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = new User("b@b.com", "asdf", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 2L);
        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoWriteService.updateTodo(authUser, todo.getId(), title, contents));

        // then
        assertEquals("해당 요청에 대한 권한이 없습니다.", exception.getMessage());
    }

    @Test
    public void 요청_필드가_비어있어서_일정_수정에_실패한다() {
        // given
        String title = "";
        String contents = "";
        String weather = weatherClient.getTodayWeather();
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = new User("b@b.com", "asdf", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        Todo todo = new Todo("title", "contents", weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoWriteService.updateTodo(authUser, todo.getId(), title, contents));

        // then
        assertEquals("공백일 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 일정_삭제에_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("title", "contents", weatherClient.getTodayWeather(), user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

        // when
        todoWriteService.deleteTodo(user.getId(), todo.getId());

        // then
        then(todoRepository).should(times(1)).delete(todo);
    }

    @Test
    public void 일정을_찾을_수_없어서_일정_삭제에_실패한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("title", "contents", weatherClient.getTodayWeather(), user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoWriteService.deleteTodo(user.getId(), todo.getId()));

        // then
        assertEquals("할 일을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 권한이_없어서_일정_삭제에_실패한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user1 = User.fromAuthUser(authUser);

        User user2 = new User("b@b.com", "asdf", UserRole.USER);
        ReflectionTestUtils.setField(user2, "id", 2L);

        Todo todo = new Todo("title", "contents", weatherClient.getTodayWeather(), user2);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoWriteService.deleteTodo(user1.getId(), todo.getId()));

        // then
        assertEquals("해당 요청에 대한 권한이 없습니다.", exception.getMessage());
    }

}
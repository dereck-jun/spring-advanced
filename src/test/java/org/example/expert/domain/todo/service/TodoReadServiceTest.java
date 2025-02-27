package org.example.expert.domain.todo.service;

import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TodoReadServiceTest {

    @Mock
    private TodoRepository repository;

    @InjectMocks
    private TodoReadService todoReadService;
    
    @Test
    public void 일정_전체_조회에_성공한다() {
        // given
        User user1 = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user1, "id", 1L);
        Todo todo1 = new Todo("title", "contents", "weather", user1);
        ReflectionTestUtils.setField(todo1, "id", 1L);
        Todo todo2 = new Todo("title", "contents", "weather", user1);
        ReflectionTestUtils.setField(todo2, "id", 2L);
        List<Todo> todos = new ArrayList<>(List.of(todo1, todo2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        given(repository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todoPage);
    
        // when
        Page<TodoResponse> result = todoReadService.getTodos(1, 10);

        // then
        assertNotNull(result);
        assertEquals(todos.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
    }

    @Test
    public void 일정_단건_조회에_성공한다() {
        // given
        User user1 = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user1, "id", 1L);
        Todo todo1 = new Todo("title", "contents", "weather", user1);
        ReflectionTestUtils.setField(todo1, "id", 1L);

        given(repository.findByIdWithUser(todo1.getId())).willReturn(Optional.of(todo1));

        // when
        TodoResponse response = todoReadService.getTodoResponse(todo1.getId());

        // then
        assertNotNull(response);
        assertEquals(todo1.getId(), response.getId());
    }
}
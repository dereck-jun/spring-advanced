package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TodoWriteService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    @Transactional
    public TodoResponse updateTodo(AuthUser authUser, long todoId, String title, String contents) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
            .orElseThrow(() -> new InvalidRequestException("할 일을 찾을 수 없습니다."));

        if (!todo.getUser().getId().equals(authUser.getId())) {
            throw new InvalidRequestException("해당 요청에 대한 권한이 없습니다.");
        }

        if (!StringUtils.hasText(title) && !StringUtils.hasText(contents)) {
            throw new InvalidRequestException("공백일 수 없습니다.");
        }

        if (title != null) {
            todo.updateTitle(title);
        }

        if (contents != null) {
            todo.updateContents(contents);
        }

        return new TodoResponse(
            todo.getId(),
            todo.getTitle(),
            todo.getContents(),
            todo.getWeather(),
            new UserResponse(authUser.getId(), authUser.getEmail()),
            todo.getCreatedAt(),
            todo.getModifiedAt()
        );
    }

    @Transactional
    public void deleteTodo(long userId, long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
            .orElseThrow(() -> new InvalidRequestException("할 일을 찾을 수 없습니다."));

        if (userId != todo.getUser().getId()) {
            throw new InvalidRequestException("해당 요청에 대한 권한이 없습니다.");
        }

        todoRepository.delete(todo);
    }
}

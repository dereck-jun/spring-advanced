package org.example.expert.domain.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoUpdateRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoReadService;
import org.example.expert.domain.todo.service.TodoWriteService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoController {

    private final TodoWriteService todoWriteService;
    private final TodoReadService todoReadService;

    @PostMapping
    public ResponseEntity<TodoSaveResponse> saveTodo(
        @Auth AuthUser authUser,
        @Valid @RequestBody TodoSaveRequest todoSaveRequest
    ) {
        return ResponseEntity.ok(todoWriteService.saveTodo(authUser, todoSaveRequest));
    }

    @GetMapping
    public ResponseEntity<Page<TodoResponse>> getTodos(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(todoReadService.getTodos(page, size));
    }

    @GetMapping("/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
        return ResponseEntity.ok(todoReadService.getTodoResponse(todoId));
    }

    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
        @Auth AuthUser authUser,
        @PathVariable long todoId,
        @Valid @RequestBody TodoUpdateRequest todoUpdateRequest
    ) {
        return ResponseEntity.ok(
            todoWriteService.updateTodo(
                authUser,
                todoId,
                todoUpdateRequest.getTitle(),
                todoUpdateRequest.getContents()
            )
        );
    }

    @DeleteMapping("/{todoId}")
    public void deleteTodo(@Auth AuthUser authUser, @PathVariable long todoId) {
        todoWriteService.deleteTodo(authUser.getId(), todoId);
    }
}

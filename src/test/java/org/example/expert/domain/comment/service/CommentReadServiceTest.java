package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentReadServiceTest {

    @Mock
    private CommentRepository repository;

    @InjectMocks
    private CommentReadService commentReadService;
    
    @Test
    public void 댓글_전체_조회에_성공한다() {
        // given
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "contents", "weather", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Comment comment1 = new Comment("contents", user, todo);
        ReflectionTestUtils.setField(comment1, "id", 1L);
        Comment comment2 = new Comment("contents", user, todo);
        ReflectionTestUtils.setField(comment2, "id", 1L);
        Comment comment3 = new Comment("contents", user, todo);
        ReflectionTestUtils.setField(comment3, "id", 1L);

        List<Comment> comments = new ArrayList<>(List.of(comment1, comment2, comment3));
        List<CommentResponse> commentResponses = comments.stream()
            .map(comment -> new CommentResponse(
                comment.getId(),
                comment.getContents(),
                new UserResponse(user.getId(), user.getEmail())
            )).toList();

        given(repository.findByTodoIdWithUser(todo.getId())).willReturn(comments);

        // when
        List<CommentResponse> responses = commentReadService.getComments(todo.getId());

        // then
        assertNotNull(responses);
        assertEquals(commentResponses.size(), responses.size());
    }
}
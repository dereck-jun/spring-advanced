package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.request.CommentUpdateRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoReadService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommentWriteServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoReadService todoReadService;
    @InjectMocks
    private CommentWriteService commentWriteService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        CommentSaveRequest request = new CommentSaveRequest(1L, "contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoReadService.getTodoOrThrow(anyLong())).willThrow(new InvalidRequestException("할 일을 찾을 수 없습니다."));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentWriteService.saveComment(authUser, request.getTodoId(), request.getContents());
        });

        // then
        assertEquals("할 일을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        CommentSaveRequest request = new CommentSaveRequest(1L, "contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(todoReadService.getTodoOrThrow(anyLong())).willReturn(todo);
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentWriteService.saveComment(authUser, request.getTodoId(), request.getContents());

        // then
        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
    }
    
    @Test
    public void 댓글_수정에_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        long commentId = 1L;
        Comment comment = new Comment("content", user, todo);
        ReflectionTestUtils.setField(comment, "id", commentId);
        CommentUpdateRequest request = new CommentUpdateRequest("contents");

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentResponse response = commentWriteService.updateComment(authUser, commentId, request.getContents());

        // then
        assertNotNull(response);
        assertEquals(comment.getId(), response.getId());
        assertEquals("contents", comment.getContents());
    }

    @Test
    public void 권한이_없어서_댓글_수정에_실패한다() {
        // given
        AuthUser authUser = new AuthUser(2L, "a@a.com", UserRole.USER);
        User user = new User("b@b.com", "pasdf", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        Todo todo = new Todo("title", "title", "contents", user);
        long commentId = 1L;
        Comment comment = new Comment("content", user, todo);
        ReflectionTestUtils.setField(comment, "id", commentId);
        CommentUpdateRequest request = new CommentUpdateRequest("contents");

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentWriteService.updateComment(authUser, commentId, request.getContents()));

        // then
        assertEquals("해당 요청에 대한 권한이 없습니다.", exception.getMessage());
    }

    @Test
    public void 댓글_삭제에_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment("content", user, todo);
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);
        // when
        commentWriteService.deleteComment(user.getId(), comment.getId());

        // then
        then(commentRepository).should(times(1)).findById(1L);
        then(commentRepository).should(times(1)).delete(comment);
    }

    @Test
    public void 권한이_없어서_댓글_삭제에_실패한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = new User("b@b.com", "asdf", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 2L);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment("content", user, todo);
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentWriteService.deleteComment(authUser.getId(), comment.getId()));

        // then
        assertEquals("해당 요청에 대한 권한이 없습니다.", exception.getMessage());
        then(commentRepository).should(times(0)).delete(comment);
    }
}

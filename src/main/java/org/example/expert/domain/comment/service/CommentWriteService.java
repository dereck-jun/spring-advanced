package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoReadService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentWriteService {

    private final TodoReadService todoReadService;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentSaveResponse saveComment(AuthUser authUser, long todoId, String contents) {
        User user = User.fromAuthUser(authUser);
        Todo todo = todoReadService.getTodoOrThrow(todoId);

        Comment newComment = new Comment(
            contents,
            user,
            todo
        );

        Comment savedComment = commentRepository.save(newComment);

        return new CommentSaveResponse(
            savedComment.getId(),
            savedComment.getContents(),
            new UserResponse(user.getId(), user.getEmail())
        );
    }

    @Transactional
    public CommentResponse updateComment(AuthUser authUser, long commentId, String contents) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new InvalidRequestException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(authUser.getId())) {
            throw new InvalidRequestException("해당 요청에 대한 권한이 없습니다.");
        }

        comment.update(contents);

        return new CommentResponse(
            comment.getId(),
            comment.getContents(),
            new UserResponse(authUser.getId(), authUser.getEmail())
        );
    }

    @Transactional
    public void deleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new InvalidRequestException("댓글을 찾을 수 없습니다."));

        if (userId != comment.getUser().getId()) {
            throw new InvalidRequestException("해당 요청에 대한 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}

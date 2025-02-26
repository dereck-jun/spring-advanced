package org.example.expert.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.request.CommentUpdateRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentReadService;
import org.example.expert.domain.comment.service.CommentWriteService;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentWriteService commentWriteService;
    private final CommentReadService commentReadService;

    @PostMapping
    public ResponseEntity<CommentSaveResponse> saveComment(
        @Auth AuthUser authUser,
        @Valid @RequestBody CommentSaveRequest commentSaveRequest
    ) {
        return ResponseEntity.ok(
            commentWriteService.saveComment(
                authUser,
                commentSaveRequest.getTodoId(),
                commentSaveRequest.getContents()
            )
        );
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@RequestParam long todoId) {
        return ResponseEntity.ok(commentReadService.getComments(todoId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
        @Auth AuthUser authUser,
        @PathVariable long commentId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        return ResponseEntity.ok(
            commentWriteService.updateComment(
                authUser,
                commentId,
                commentUpdateRequest.getContents()
            )
        );
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
        @Auth AuthUser authUser,
        @PathVariable long commentId
    ) {
        commentWriteService.deleteComment(authUser.getId(), commentId);
    }
}

package org.example.expert.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.common.annotation.LogTrace;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @LogTrace
    @DeleteMapping("/admin/comments/{commentId}")
    public void deleteComment(@PathVariable long commentId) {
        commentAdminService.deleteComment(commentId);
    }
}

package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    @Mock
    private CommentRepository repository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    public void 댓글_삭제에_성공한다() {
        // given
        long commentId = 1L;
        doNothing().when(repository).deleteById(commentId);

        // when
        commentAdminService.deleteComment(commentId);

        // then
        then(repository).should(times(1)).deleteById(1L);
    }


}
package org.example.expert.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentSaveRequest {

    @NotNull    // 문자열 타입에만 @NotBlank 적용됨;;
    private Long todoId;

    @NotBlank
    private String contents;
}

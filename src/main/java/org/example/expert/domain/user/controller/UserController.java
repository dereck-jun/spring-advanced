package org.example.expert.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserReadService;
import org.example.expert.domain.user.service.UserWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserWriteService userWriteService;
    private final UserReadService userReadService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userReadService.getUserResponse(userId));
    }

    @PutMapping
    public void changePassword(@Auth AuthUser authUser, @Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userWriteService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@Auth AuthUser authUser, @PathVariable long userId) {
        userWriteService.deleteUser(authUser.getId(), userId);
    }
}

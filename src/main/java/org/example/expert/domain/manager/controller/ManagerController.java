package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.util.JwtUtil;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerReadService;
import org.example.expert.domain.manager.service.ManagerWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/managers")
public class ManagerController {

    private final ManagerWriteService managerWriteService;
    private final ManagerReadService managerReadService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ManagerSaveResponse> saveManager(
            @Auth AuthUser authUser,
            @Valid @RequestBody ManagerSaveRequest managerSaveRequest
    ) {
        return ResponseEntity.ok(
            managerWriteService.saveManager(
                authUser,
                managerSaveRequest.getTodoId(),
                managerSaveRequest.getManagerUserId()
            )
        );
    }

    @GetMapping
    public ResponseEntity<List<ManagerResponse>> getMembers(@RequestParam long todoId) {
        return ResponseEntity.ok(managerReadService.getManagers(todoId));
    }

    @DeleteMapping("/{managerId}")
    public void deleteManager(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable long managerId
    ) {
        Claims claims = jwtUtil.extractClaims(bearerToken.substring(7));
        long userId = Long.parseLong(claims.getSubject());
        managerWriteService.deleteManager(userId, managerId);
    }
}

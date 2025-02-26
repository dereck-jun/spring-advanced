package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserReadService userReadService;

    @Transactional
    public void changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {
        User user = userReadService.getUserOrThrow(userId);
        user.updateRole(UserRole.of(userRoleChangeRequest.getRole()));
    }
}

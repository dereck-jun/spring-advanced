package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

    private final UserRepository userRepository;

    public UserResponse getUserResponse(long userId) {
        User user = getUserOrThrow(userId);
        return new UserResponse(user.getId(), user.getEmail());
    }

    public User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다."));
    }
}

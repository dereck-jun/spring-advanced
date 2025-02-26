package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoReadService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.service.UserReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ManagerWriteService {

    private final ManagerRepository managerRepository;
    private final UserReadService userReadService;
    private final TodoReadService todoReadService;

    @Transactional
    public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, long managerUserId) {
        // 일정을 만든 유저
        User user = User.fromAuthUser(authUser);
        Todo todo = todoReadService.getTodoOrThrow(todoId);

        if (todo.getUser() == null) {
            throw new InvalidRequestException("일정을 만든 유저를 찾을 수 없습니다.");
        }

        if (!ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("담당자를 등록하려고 하는 유저와 일정을 만든 유저가 유효하지 않습니다.");
        }

        User managerUser = userReadService.getUserOrThrow(managerUserId);

        if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {
            throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
        }

        Manager newManagerUser = new Manager(managerUser, todo);
        Manager savedManagerUser = managerRepository.save(newManagerUser);

        return new ManagerSaveResponse(
            savedManagerUser.getId(),
            new UserResponse(managerUser.getId(), managerUser.getEmail())
        );
    }

    @Transactional
    public void deleteManager(long userId, long managerId) {

        Manager manager = managerRepository.findById(managerId)
            .orElseThrow(() -> new InvalidRequestException("매니저를 찾을 수 없습니다."));

        Todo todo = todoReadService.getTodoOrThrow(manager.getTodo().getId());

        if (todo.getUser() == null) {
            throw new InvalidRequestException("일정을 만든 유저를 찾을 수 없습니다.");
        }

        User user = userReadService.getUserOrThrow(userId);

        if (!ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("해당 일정을 만든 유저가 유효하지 않습니다.");
        }

        if (!ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId())) {
            throw new InvalidRequestException("해당 일정에 등록된 담당자가 아닙니다.");
        }

        managerRepository.delete(manager);
    }
}

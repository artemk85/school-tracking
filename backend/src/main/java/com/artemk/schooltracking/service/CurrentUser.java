package com.artemk.schooltracking.service;

import com.artemk.schooltracking.config.AppUserPrincipal;
import com.artemk.schooltracking.domain.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUser {

    public AppUserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
        }
        return principal;
    }

    public Long userId() {
        return principal().getId();
    }

    public boolean isParent() {
        return Role.PARENT.name().equals(principal().getRole());
    }

    public boolean isChild() {
        return Role.CHILD.name().equals(principal().getRole());
    }

    /**
     * Владелец данных (общих предметов, настроек): для родителя — он сам,
     * для ребёнка — его родитель.
     */
    public Long ownerId() {
        AppUserPrincipal principal = principal();
        if (isParent()) {
            return principal.getId();
        }
        Long parentId = principal.getParentId();
        if (parentId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "У ребёнка не указан родитель");
        }
        return parentId;
    }

    /**
     * Ребёнок, к оценкам которого относится запрос. Ребёнок всегда видит только
     * себя, родитель обязан указать childId.
     */
    public Long effectiveChildId(Long requestedChildId) {
        if (isChild()) {
            return userId();
        }
        if (requestedChildId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан ребёнок");
        }
        return requestedChildId;
    }
}
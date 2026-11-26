package com.cms.complaint.service;

import com.cms.complaint.dto.NotificationResponse;
import com.cms.complaint.entity.Notification;
import com.cms.complaint.entity.User;
import com.cms.complaint.enums.Role;
import com.cms.complaint.repository.NotificationRepository;
import com.cms.complaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    public void notify(User user, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message.length() > 300 ? message.substring(0, 300) : message);
        notifications.save(n);
    }

    public void notifyAdmins(String message) {
        users.findByRole(Role.ADMIN).forEach(a -> notify(a, message));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> list = notifications.findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setRead(true));
        notifications.saveAll(list);
    }
}

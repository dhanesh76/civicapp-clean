package com.visioners.civic.complaint.notification;

import org.springframework.stereotype.Service;

import com.visioners.civic.complaint.dto.notification.ComplaintNotification;
import com.visioners.civic.complaint.model.NotificationType;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
public class ComplaintNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("asyncExecutor")
    public void notifyDepartment(String complaintId, Long deptId, NotificationType type) {
        send("/topic/department/" + deptId, type, complaintId);
    }

    @Async("asyncExecutor")
    public void notifyUser(String complaintId, Long userId, NotificationType type) {
        send("/topic/user/" + userId, type, complaintId);
    }

    @Async("asyncExecutor")
    public void notifyFieldWorker(String complaintId, Long workerId, NotificationType type) {
        send("/topic/worker/" + workerId, type, complaintId);
    }

    private void send(String destination, NotificationType type, String complaintId) {
        messagingTemplate.convertAndSend(
            destination,
            new ComplaintNotification(type.name(), complaintId)
        );
    }
}

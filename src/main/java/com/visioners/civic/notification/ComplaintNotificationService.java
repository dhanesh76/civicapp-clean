package com.visioners.civic.notification;

import org.springframework.stereotype.Service;

import com.visioners.civic.complaint.dto.notification.ComplaintNotification;
import com.visioners.civic.complaint.model.NotificationType;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
public class ComplaintNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("asyncExecutor")
    public void notifyDepartmentOfficer(String complaintId, Long departmentId, NotificationType type) {
        send("/topic/department/" + departmentId, type, complaintId);
    }

    @Async("asyncExecutor")
    public void notifyUser(String complaintId, Long userId, NotificationType type) {
        send("/topic/user/" + userId, type, complaintId);
    }

    @Async("asyncExecutor")
    public void notifyFieldWorker(String complaintId, Long workerId, NotificationType type) {
        send("/topic/worker/" + workerId, type, complaintId);
    }

    private void send(@NonNull String destination, NotificationType type, String complaintId) {
        
        Objects.requireNonNull(destination);

        messagingTemplate.convertAndSend(
            destination,
            new ComplaintNotification(type.name(), complaintId)
        );
    }
}

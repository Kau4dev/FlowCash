package com.kau4dev.notification.infra.queue;

import com.kau4dev.notification.model.dto.TransferNotificationDTO;
import com.kau4dev.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.name:notification-queue}")
    public void receiveNotification(TransferNotificationDTO notification) {
        log.info("Received notification: payer={}, payee={}, amount={}",
                notification.payerId(), notification.payeeId(), notification.amount());

        try {
            notificationService.sendNotification(notification);
            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            // Aqui você pode implementar retry ou DLQ (Dead Letter Queue)
        }
    }
}

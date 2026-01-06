package com.kau4dev.notification.service;

import com.kau4dev.notification.model.dto.TransferNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void sendNotification(TransferNotificationDTO transferNotificationDTO) {
        log.info("Sending email to payer {} and payee {}", transferNotificationDTO.payerId(), transferNotificationDTO.payeeId());



        log.info("Email sent successfully for transfer amount: {}", transferNotificationDTO.amount());
    }
}

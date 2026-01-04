package com.kau4dev.transfer.infra.queue;

import com.kau4dev.transfer.model.dto.TransferDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:transfer-exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key.notification:notification.routing.key}")
    private String routingKey;

    public void sendNotification(TransferDTO transfer) {
        log.info("Sending notification for transfer: payer={}, payee={}, amount={}",
                transfer.payerId(), transfer.payeeId(), transfer.amount());
        rabbitTemplate.convertAndSend(exchange, routingKey, transfer);
        log.info("Notification sent successfully to queue");
    }
}

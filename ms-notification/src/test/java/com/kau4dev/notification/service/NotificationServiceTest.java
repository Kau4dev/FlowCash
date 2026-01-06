package com.kau4dev.notification.service;

import com.kau4dev.notification.model.dto.TransferNotificationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private final NotificationService service = new NotificationService();

    @Mock
    private TransferNotificationDTO dto;

    @Test
    void sendNotification_naoDeveLancarExcecao() {
        when(dto.payerId()).thenReturn(UUID.randomUUID());
        when(dto.payeeId()).thenReturn(UUID.randomUUID());
        when(dto.amount()).thenReturn(BigDecimal.valueOf(100));

        assertDoesNotThrow(() -> service.sendNotification(dto));
    }
}

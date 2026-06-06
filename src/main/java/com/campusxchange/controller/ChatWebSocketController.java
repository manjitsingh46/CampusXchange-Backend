package com.campusxchange.controller;

import com.campusxchange.dto.CreateMessageRequest;
import com.campusxchange.dto.MessageDTO;
import com.campusxchange.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat/send")
    public void sendMessage(
            @Payload CreateMessageRequest request,
            @Header(value = "X-User-Id", required = false) Long senderId) {

        if (senderId == null) {
            log.warn("WebSocket message received without X-User-Id header — dropping");
            return;
        }

        MessageDTO saved = messageService.sendMessage(senderId, request);

        // Room key: sorted IDs so both sides subscribe to the same topic
        long uid1 = Math.min(senderId, request.getRecipientId());
        long uid2 = Math.max(senderId, request.getRecipientId());
        messagingTemplate.convertAndSend("/topic/chat/" + uid1 + "_" + uid2, saved);
    }
}

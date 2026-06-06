package com.campusxchange.controller;

import com.campusxchange.dto.CreateMessageRequest;
import com.campusxchange.dto.MessageDTO;
import com.campusxchange.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(userId, request));
    }

    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long partnerId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(messageService.getConversation(userId, partnerId, pageable));
    }

    @GetMapping
    public ResponseEntity<?> getUserMessages(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getUserMessages(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getUnreadMessages(userId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getConversationPartners(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }
}

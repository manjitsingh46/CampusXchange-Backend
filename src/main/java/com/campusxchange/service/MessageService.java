package com.campusxchange.service;

import com.campusxchange.dto.CreateMessageRequest;
import com.campusxchange.dto.MessageDTO;
import com.campusxchange.entity.Message;
import com.campusxchange.entity.User;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.MessageRepository;
import com.campusxchange.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;
    @Transactional
    public MessageDTO sendMessage(Long senderId, CreateMessageRequest request) {
        log.info("Sending message from user {} to user {}", senderId, request.getRecipientId());

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException(
                        "Sender not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ApiException(
                        "Recipient not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .voiceNoteUrl(request.getVoiceNoteUrl())
                .isRead(false)
                .build();

        message = messageRepository.save(message);
        log.info("Message sent with id: {}", message.getId());

        return mapToDTO(message);
    }
    @Transactional(readOnly = true)
    public Page<MessageDTO> getConversation(Long userId, Long conversationPartnerId, Pageable pageable) {
        log.info("Getting conversation between user {} and {}", userId, conversationPartnerId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        userRepository.findById(conversationPartnerId)
                .orElseThrow(() -> new ApiException(
                        "Conversation partner not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return messageRepository.findConversation(userId, conversationPartnerId, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<MessageDTO> getUserMessages(Long userId, Pageable pageable) {
        log.info("Getting all messages for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return messageRepository.findMessagesForUser(userId, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public List<MessageDTO> getUnreadMessages(Long userId) {
        log.info("Getting unread messages for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return messageRepository.findUnreadMessagesForUser(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    @Transactional
    public MessageDTO markAsRead(Long messageId) {
        log.info("Marking message as read: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(
                        "Message not found",
                        HttpStatus.NOT_FOUND.value(),
                        "MESSAGE_NOT_FOUND"
                ));

        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        message = messageRepository.save(message);

        return mapToDTO(message);
    }
    @Transactional
    public void deleteMessage(Long messageId) {
        log.info("Deleting message: {}", messageId);

        if (!messageRepository.existsById(messageId)) {
            throw new ApiException(
                    "Message not found",
                    HttpStatus.NOT_FOUND.value(),
                    "MESSAGE_NOT_FOUND"
            );
        }

        messageRepository.deleteById(messageId);
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConversationPartners(Long userId) {
        log.info("Getting conversation partners for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        List<Long> partnerIds = messageRepository.findConversationPartnerIds(userId);

        return partnerIds.stream()
                .map(partnerId -> userRepository.findById(partnerId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(user -> {
                    Map<String, Object> partner = new HashMap<>();
                    partner.put("userId", user.getId());
                    partner.put("username", user.getUsername());
                    partner.put("fullName", user.getFullName());
                    partner.put("profilePhotoUrl", user.getProfilePhotoUrl());
                    partner.put("unreadCount", messageRepository.countUnreadFromSender(userId, user.getId()));
                    return partner;
                })
                .toList();
    }
    private MessageDTO mapToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .recipientId(message.getRecipient().getId())
                .recipientName(message.getRecipient().getFullName())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .voiceNoteUrl(message.getVoiceNoteUrl())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}

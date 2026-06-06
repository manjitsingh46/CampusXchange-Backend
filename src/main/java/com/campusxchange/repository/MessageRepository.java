package com.campusxchange.repository;

import com.campusxchange.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId OR m.recipient.id = :userId)
        AND ((m.sender.id = :userId AND m.recipient.id = :recipientId)
             OR (m.sender.id = :recipientId AND m.recipient.id = :userId))
        ORDER BY m.createdAt DESC
    """)
    Page<Message> findConversation(
        @Param("userId") Long userId,
        @Param("recipientId") Long recipientId,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM Message m
        WHERE m.recipient.id = :userId AND m.isRead = false
        ORDER BY m.createdAt DESC
    """)
    List<Message> findUnreadMessagesForUser(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Message m
        WHERE m.recipient.id = :userId
        ORDER BY m.createdAt DESC
    """)
    Page<Message> findMessagesForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT DISTINCT CASE
            WHEN m.sender.id = :userId THEN m.recipient.id
            ELSE m.sender.id
        END
        FROM Message m
        WHERE m.sender.id = :userId OR m.recipient.id = :userId
        ORDER BY m.createdAt DESC
    """)
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.sender.id = :senderId AND m.isRead = false")
    long countUnreadFromSender(@Param("userId") Long userId, @Param("senderId") Long senderId);
}

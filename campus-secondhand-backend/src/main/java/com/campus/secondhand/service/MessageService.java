package com.campus.secondhand.service;

import com.campus.secondhand.dto.request.MessageSendRequest;
import com.campus.secondhand.dto.response.MessageResponse;
import com.campus.secondhand.entity.Message;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.exception.BusinessException;
import com.campus.secondhand.mapper.MessageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Transactional
    public Long send(Long senderId, MessageSendRequest request) {
        User receiver = userMapper.selectById(request.getReceiverId());
        if (receiver == null) {
            throw BusinessException.of(404, "接收者不存在");
        }

        if (senderId.equals(request.getReceiverId())) {
            throw BusinessException.of(400, "不能给自己发送消息");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setProductId(request.getProductId());
        message.setContent(request.getContent());
        message.setIsRead((byte) 0);

        messageMapper.insert(message);
        return message.getId();
    }

    public List<MessageResponse> getConversation(Long userId, Long otherUserId) {
        User otherUser = userMapper.selectById(otherUserId);
        if (otherUser == null) {
            throw BusinessException.of(404, "用户不存在");
        }

        List<Message> messages = messageMapper.selectConversation(userId, otherUserId);
        List<MessageResponse> responses = new ArrayList<>();
        
        for (Message message : messages) {
            responses.add(buildMessageResponse(message));
        }

        // Mark all messages from other user as read
        messageMapper.markConversationAsRead(userId, otherUserId);

        return responses;
    }

    public List<Map<String, Object>> getMessageList(Long userId) {
        List<Message> messages = messageMapper.selectMessageList(userId);
        
        // Group messages by conversation (other user)
        Map<Long, Map<String, Object>> conversationMap = new LinkedHashMap<>();
        
        for (Message message : messages) {
            Long otherUserId = message.getSenderId().equals(userId) ? message.getReceiverId() : message.getSenderId();
            
            if (!conversationMap.containsKey(otherUserId)) {
                User otherUser = userMapper.selectById(otherUserId);
                if (otherUser == null) continue;
                
                Map<String, Object> conversation = new HashMap<>();
                conversation.put("userId", otherUserId);
                conversation.put("nickname", otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
                conversation.put("avatar", otherUser.getAvatarUrl());
                conversation.put("lastMessage", message.getContent());
                conversation.put("lastTime", message.getCreateTime());
                conversation.put("unreadCount", 0);
                
                conversationMap.put(otherUserId, conversation);
            } else {
                Map<String, Object> conversation = conversationMap.get(otherUserId);
                // Update last message if this message is newer
                if (message.getCreateTime().isAfter((java.time.LocalDateTime) conversation.get("lastTime"))) {
                    conversation.put("lastMessage", message.getContent());
                    conversation.put("lastTime", message.getCreateTime());
                }
            }
            
            // Count unread messages (only messages received, not sent)
            if (message.getReceiverId().equals(userId) && message.getIsRead() == 0) {
                Map<String, Object> conversation = conversationMap.get(otherUserId);
                conversation.put("unreadCount", (Integer) conversation.get("unreadCount") + 1);
            }
        }
        
        return new ArrayList<>(conversationMap.values());
    }

    public void markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw BusinessException.of(404, "消息不存在");
        }

        if (!message.getReceiverId().equals(userId)) {
            throw BusinessException.of(403, "只能标记自己收到的消息为已读");
        }

        messageMapper.markAsRead(messageId);
    }

    public int getUnreadCount(Long userId) {
        return messageMapper.countUnread(userId);
    }

    private MessageResponse buildMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setProductId(message.getProductId());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setCreateTime(message.getCreateTime());

        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            response.setSenderNickname(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            response.setSenderAvatar(sender.getAvatarUrl());
        }

        User receiver = userMapper.selectById(message.getReceiverId());
        if (receiver != null) {
            response.setReceiverNickname(receiver.getNickname() != null ? receiver.getNickname() : receiver.getUsername());
            response.setReceiverAvatar(receiver.getAvatarUrl());
        }

        if (message.getProductId() != null) {
            Product product = productMapper.selectById(message.getProductId());
            if (product != null) {
                response.setProductTitle(product.getTitle());
            }
        }

        return response;
    }
}
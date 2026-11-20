package com.example.elderly.service;

import com.example.elderly.dto.chat.ChatCreateConversationDTO;
import com.example.elderly.dto.chat.ChatSendMessageDTO;
import com.example.elderly.entity.Message;
import com.example.elderly.entity.MessageAttachment;
import com.example.elderly.vo.chat.ConversationVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    List<ConversationVO> listConversations(Long userId);

    List<Message> listMessages(Long conversationId, Long userId);

    Message sendMessage(Long conversationId, ChatSendMessageDTO dto, Long userId);

    Long createConversation(ChatCreateConversationDTO dto, Long userId);

    void markRead(Long conversationId, Long userId);

    MessageAttachment uploadAttachment(MultipartFile file);
}

package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.dto.chat.ChatCreateConversationDTO;
import com.example.elderly.dto.chat.ChatSendMessageDTO;
import com.example.elderly.entity.Message;
import com.example.elderly.entity.MessageAttachment;
import com.example.elderly.service.ChatService;
import com.example.elderly.util.SecurityUtil;
import com.example.elderly.vo.chat.ConversationVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "聊天会话")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        Long userId = SecurityUtil.getLoginUserId();
        return Result.success(chatService.listConversations(userId));
    }

    @PostMapping("/conversations")
    public Result<Long> create(@Validated @RequestBody ChatCreateConversationDTO dto) {
        Long userId = SecurityUtil.getLoginUserId();
        return Result.success(chatService.createConversation(dto, userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<Message>> messages(@PathVariable Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        return Result.success(chatService.listMessages(id, userId));
    }

    @PostMapping("/conversations/{id}/messages")
    public Result<Message> send(@PathVariable Long id, @Validated @RequestBody ChatSendMessageDTO dto) {
        Long userId = SecurityUtil.getLoginUserId();
        return Result.success(chatService.sendMessage(id, dto, userId));
    }

    @PutMapping("/conversations/{id}/read")
    public Result<Boolean> read(@PathVariable Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        chatService.markRead(id, userId);
        return Result.success(true);
    }

    @PostMapping("/upload")
    public Result<MessageAttachment> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(chatService.uploadAttachment(file));
    }
}

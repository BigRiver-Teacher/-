package com.example.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.entity.Conversation;
import com.example.elderly.entity.Message;
import com.example.elderly.entity.MessageAttachment;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.ConversationMapper;
import com.example.elderly.mapper.MessageAttachmentMapper;
import com.example.elderly.mapper.MessageMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员对话监管：只读查看全部会话（专家↔家属/老人）与消息流。
 */
@Tag(name = "管理员对话监管")
@RestController
@RequestMapping("/api/admin/chat")
public class AdminChatController {

    @Resource
    private ConversationMapper conversationMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageAttachmentMapper attachmentMapper;
    @Resource
    private UserMapper userMapper;

    /** 全部会话列表（不限登录人，按最后消息时间倒序） */
    @GetMapping("/conversations")
    public Result<List<Conversation>> conversations() {
        checkAdmin();
        List<Conversation> list = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getIsDeleted, 0)
                .orderByDesc(Conversation::getLastMessageTime));
        return Result.success(list);
    }

    /** 某会话的消息流（含附件） */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<Message>> messages(@PathVariable Long id) {
        checkAdmin();
        List<Message> msgs = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, id)
                .orderByAsc(Message::getCreateTime));
        for (Message m : msgs) {
            m.setAttachments(attachmentMapper.selectList(new LambdaQueryWrapper<MessageAttachment>()
                    .eq(MessageAttachment::getMessageId, m.getId())));
        }
        return Result.success(msgs);
    }

    /** 仅管理员/员工可访问对话监管 */
    private void checkAdmin() {
        Long userId = SecurityUtil.getLoginUserId();
        User user = userMapper.selectById(userId);
        if (user == null || (!"admin".equals(user.getRole()) && !"staff".equals(user.getRole()))) {
            throw new BusinessException(403, "无权限访问对话监管");
        }
    }
}

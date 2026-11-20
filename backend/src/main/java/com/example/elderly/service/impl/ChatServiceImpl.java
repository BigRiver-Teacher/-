package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.dto.chat.ChatCreateConversationDTO;
import com.example.elderly.dto.chat.ChatSendMessageDTO;
import com.example.elderly.entity.Conversation;
import com.example.elderly.entity.Elder;
import com.example.elderly.entity.Message;
import com.example.elderly.entity.MessageAttachment;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.ConversationMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.mapper.MessageAttachmentMapper;
import com.example.elderly.mapper.MessageMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.ChatService;
import com.example.elderly.vo.chat.ConversationVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ConversationMapper conversationMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageAttachmentMapper attachmentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ElderMapper elderMapper;
    @Resource
    private NotificationHelper notificationHelper;

    /** 附件根目录，与 WebConfig 的 /uploads/** 映射使用同一配置项。 */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        List<Conversation> conversations = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getFamilyUserId, userId).or().eq(Conversation::getServiceUserId, userId))
                .eq(Conversation::getIsDeleted, 0)
                .orderByDesc(Conversation::getLastMessageTime));
        return conversations.stream().map(c -> {
            ConversationVO vo = new ConversationVO();
            vo.setId(c.getId());
            vo.setType(c.getType());
            vo.setElderId(c.getElderId());
            vo.setElderName(c.getElderName());
            vo.setLastMessage(c.getLastMessage());
            vo.setLastMessageTime(c.getLastMessageTime());
            boolean isFamily = userId.equals(c.getFamilyUserId());
            vo.setPeerName(isFamily ? c.getServiceName() : c.getFamilyName());
            User peerUser = userMapper.selectById(isFamily ? c.getServiceUserId() : c.getFamilyUserId());
            vo.setPeerRole(peerUser != null ? peerUser.getRole() : (isFamily ? "expert" : "elder"));
            Long unread = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getConversationId, c.getId())
                    .eq(Message::getIsRead, 0)
                    .ne(Message::getSenderId, userId));
            vo.setUnreadCount(unread);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Message> listMessages(Long conversationId, Long userId) {
        checkAccess(conversationId, userId);
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreateTime));
        for (Message m : messages) {
            List<MessageAttachment> atts = attachmentMapper.selectList(new LambdaQueryWrapper<MessageAttachment>()
                    .eq(MessageAttachment::getMessageId, m.getId()));
            m.setAttachments(atts);
        }
        return messages;
    }

    @Override
    @Transactional
    public Message sendMessage(Long conversationId, ChatSendMessageDTO dto, Long userId) {
        Conversation c = checkAccess(conversationId, userId);
        User sender = userMapper.selectById(userId);
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(userId);
        msg.setSenderRole(sender != null ? sender.getRole() : "elder");
        msg.setSenderName(sender != null ? sender.getRealName() : "");
        msg.setContent(dto.getContent());
        msg.setContentType(dto.getContentType() == null ? "TEXT" : dto.getContentType());
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        List<MessageAttachment> atts = new ArrayList<>();
        if (dto.getAttachments() != null) {
            for (ChatSendMessageDTO.AttachmentItem item : dto.getAttachments()) {
                MessageAttachment att = new MessageAttachment();
                att.setMessageId(msg.getId());
                att.setConversationId(conversationId);
                att.setFileName(item.getFileName());
                att.setFileType(item.getFileType());
                att.setFilePath(item.getFilePath());
                att.setFileSize(item.getFileSize());
                att.setCreateTime(LocalDateTime.now());
                attachmentMapper.insert(att);
                atts.add(att);
            }
        }
        msg.setAttachments(atts);
        c.setLastMessage(dto.getContent());
        c.setLastMessageTime(LocalDateTime.now());
        conversationMapper.updateById(c);

        // 站内通知：新消息通知接收方
        Long receiverId = userId.equals(c.getFamilyUserId()) ? c.getServiceUserId() : c.getFamilyUserId();
        if (receiverId != null && !receiverId.equals(userId)) {
            String link;
            if ("CAREGIVER".equals(c.getType())) {
                link = receiverId.equals(c.getServiceUserId()) ? "/caregiver/workbench" : "/elder/caregiver";
            } else {
                link = receiverId.equals(c.getServiceUserId()) ? "/expert/consult" : "/elder/consult";
            }
            notificationHelper.notify(receiverId, "收到新消息",
                    (sender != null ? sender.getRealName() : "") + "：" + (dto.getContent() != null ? dto.getContent() : "[图片]"),
                    "CHAT", link);
        }
        return msg;
    }

    @Override
    public Long createConversation(ChatCreateConversationDTO dto, Long userId) {
        Conversation exist = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getElderId, dto.getElderId())
                .eq(Conversation::getServiceUserId, dto.getServiceUserId())
                .eq(Conversation::getType, dto.getType())
                .eq(Conversation::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (exist != null) {
            return exist.getId();
        }
        User familyUser = userMapper.selectById(userId);
        User serviceUser = userMapper.selectById(dto.getServiceUserId());
        Elder elder = elderMapper.selectById(dto.getElderId());
        Conversation c = new Conversation();
        c.setType(dto.getType());
        c.setElderId(dto.getElderId());
        c.setElderName(elder != null ? elder.getName() : "");
        c.setFamilyUserId(userId);
        c.setFamilyName(familyUser != null ? familyUser.getRealName() : "");
        c.setServiceUserId(dto.getServiceUserId());
        c.setServiceId(dto.getServiceId());
        c.setServiceName(serviceUser != null ? serviceUser.getRealName() : "");
        c.setStatus("OPEN");
        c.setCreateTime(LocalDateTime.now());
        c.setIsDeleted(0);
        conversationMapper.insert(c);
        return c.getId();
    }

    @Override
    public void markRead(Long conversationId, Long userId) {
        checkAccess(conversationId, userId);
        LambdaUpdateWrapper<Message> update = new LambdaUpdateWrapper<>();
        update.eq(Message::getConversationId, conversationId)
                .ne(Message::getSenderId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        messageMapper.update(null, update);
    }

    @Override
    public MessageAttachment uploadAttachment(MultipartFile file) {
        // 附件目录由 app.upload.dir 控制（默认 uploads/），与 /uploads/** 静态映射保持一致
        File base = new File(uploadDir);
        if (!base.isAbsolute()) {
            base = new File(System.getProperty("user.dir"), uploadDir);
        }
        File dirFile = new File(base, "attachments");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        String dir = "uploads/attachments";
        String originalName = file.getOriginalFilename();
        String savedName = System.currentTimeMillis() + "_" + (originalName != null ? originalName : "file");
        try {
            file.transferTo(new File(dirFile, savedName));
        } catch (Exception e) {
            throw new BusinessException(500, "文件保存失败");
        }
        MessageAttachment att = new MessageAttachment();
        att.setFileName(originalName);
        att.setFileType(file.getContentType());
        att.setFilePath(dir + "/" + savedName);
        att.setFileSize(file.getSize());
        return att;
    }

    private Conversation checkAccess(Long conversationId, Long userId) {
        Conversation c = conversationMapper.selectById(conversationId);
        if (c == null || (!userId.equals(c.getFamilyUserId()) && !userId.equals(c.getServiceUserId()))) {
            throw new BusinessException(403, "无权访问该会话");
        }
        return c;
    }
}

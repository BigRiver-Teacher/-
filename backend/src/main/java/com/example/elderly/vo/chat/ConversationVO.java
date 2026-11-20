package com.example.elderly.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private String type;
    private Long elderId;
    private String elderName;
    /** 对方姓名（当前用户为家属时是服务方，反之是家属） */
    private String peerName;
    private String peerRole;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}

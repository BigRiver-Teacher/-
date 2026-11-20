package com.example.elderly.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatCreateConversationDTO {
    @NotBlank(message = "会话类型不能为空")
    private String type;
    @NotNull(message = "老人ID不能为空")
    private Long elderId;
    @NotNull(message = "服务方账号ID不能为空")
    private Long serviceUserId;
    private Long serviceId;
}

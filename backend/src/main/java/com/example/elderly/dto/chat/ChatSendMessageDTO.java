package com.example.elderly.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatSendMessageDTO {
    @NotBlank(message = "消息内容不能为空")
    private String content;
    private String contentType = "TEXT";
    private List<AttachmentItem> attachments;

    @Data
    public static class AttachmentItem {
        private String fileName;
        private String fileType;
        private String filePath;
        private Long fileSize;
    }
}

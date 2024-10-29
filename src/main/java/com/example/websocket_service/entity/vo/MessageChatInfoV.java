package com.example.websocket_service.entity.vo;

import lombok.Data;

@Data
public class MessageChatInfoV {
    private String flag;
    private String createdUser;
    private String roomId;
    private String userId;
    private String userName;
    private String fileName;
    private Long fileSize;
    private String status;
    private Integer userType;
    private String date;
    private String content;
    private Integer type;
}

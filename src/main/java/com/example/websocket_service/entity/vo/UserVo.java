package com.example.websocket_service.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserVo {
    private String roomId;
    private String userName;
    private String createdTime;
}

package com.example.websocket_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long id;
    private String userId;
    private String userName;
    private Integer userType;
    private Integer type;
    private String roomId;
    private String status;
    private String content;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdTime;
    private String createdUser;
}

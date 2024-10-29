package com.example.websocket_service.entity.vo;

import com.example.websocket_service.entity.Message;
import lombok.Data;

import java.util.List;

@Data
public class MessageInfoV {
//    private List<Message> messageList;
    private String roomId;
    private String userId;
    private String userName;
    private Integer waitCustomer;
    private Integer waitTime;
    private String status;
}

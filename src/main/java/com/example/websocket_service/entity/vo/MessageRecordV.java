package com.example.websocket_service.entity.vo;

import com.example.websocket_service.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRecordV extends Message {
//    private byte[] picData;
    private String vId;
    private String fileName;
    private Long fileSize;
}

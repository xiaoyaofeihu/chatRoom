package com.example.websocket_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintInfo {
    //订单号
    private String orderNumber;
    //投诉人
    private String complainant;
    //投诉事件
    private String complaints;
    //投诉详细信息
    private String complaintDetails;
    //状态
    private Integer state;
    //受理人
    private String acceptor;
    //受理时间
    private String processingTime;
    //图片
    private String picture;
    //创建人
    private String creator;
    //创建时间
    private String createDate;
    //编辑人
    private String editor;
    //编辑人id
    private String editorId;
    //编辑时间
    private String editorDate;
    //投诉人id
    private String complainantId;
}
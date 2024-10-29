package com.example.websocket_service.entity.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintInfoF implements Serializable {
    private String orderNumber;
    private String complainant;
    private String complainantId;
    private List<String> complaints;
    private String complaintDetails;
    private String picture;
    private MultipartFile picture1;
}
package com.example.websocket_service.entity.form;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
public class UserServiceF {
    private String roomId;
    private String userName;
    private String customerName;
    private String createdTime;
    private String beforeTime;
    @NotNull(message="curPage不能为null")
    private Integer curPage;
    @NotNull(message="pageSize不能为null")
    private Integer pageSize;
}

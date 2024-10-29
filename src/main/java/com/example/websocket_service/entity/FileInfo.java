package com.example.websocket_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo implements Serializable {

    private static final long serialVersionID = 1L;

    private Integer id;

    private String name;

    private String type;

    private Long size;

    private String url;

    private String md5;

    private Integer isDelete;


    private String userId; // 归属人

    private String createdTime;

    private String updatedTime;

}
package com.example.websocket_service.entity.emuns;
public enum TypeEnum {
    CUSTOMERTYPE(1,"客户"),
    CUSTOMERSERVICETYPE(2,"客服"),
    MESSAGEPICTYPE(2,"图片类型"),
    MESSAGEFILETYPE(1,"文件类型"),
    MESSAGETYPE(0,"普通文本");
    private Integer code;
    private String name;

    TypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

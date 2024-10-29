package com.example.websocket_service.entity.emuns;

public enum WebSocketEnum {
    JOINROOM("joinRoom","加入房间"),
    CHATROOM("chatRoom","开始聊天"),
    EXITROOM("exitRoom","退出房间"),
    CREATEROOM("createRoom","创建房间"),
    ADMIN("admin","管理员");
    private String code;
    private String name;

    WebSocketEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.example.websocket_service.service;

import com.example.websocket_service.entity.Message;
import com.example.websocket_service.entity.vo.MessageRecordV;
import com.example.websocket_service.entity.form.UserServiceF;
import com.example.websocket_service.entity.vo.PageBean;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService {

    void insert(Message message);

    List<Message> getMessages(String userId);

    PageBean<MessageRecordV> getServiceList(UserServiceF form);

    void updateUrl(String userId, String url);

    void delete(Long id);
}

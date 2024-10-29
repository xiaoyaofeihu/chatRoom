package com.example.websocket_service.dao;


import com.example.websocket_service.entity.Message;
import com.example.websocket_service.entity.vo.MessageRecordV;
import com.example.websocket_service.entity.form.UserServiceF;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageDao {
    List<Message> getMessages(String userId);

    void insert(Message message);

    List<MessageRecordV> getServiceList(UserServiceF form);

    void updateUrl(@Param("userId") String userId,@Param("url") String url);

    void delete(Long id);

    List<MessageRecordV> getHistoryList(@Param("userName") String userName, @Param("customerName") String customerName,@Param("createdTime") String createdTime);
}

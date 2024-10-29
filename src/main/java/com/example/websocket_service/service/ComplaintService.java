package com.example.websocket_service.service;

import com.example.websocket_service.entity.ComplaintInfo;
import com.example.websocket_service.entity.form.ComplaintInfoF;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public interface ComplaintService {

    Boolean insert(ComplaintInfoF form) throws Exception;

    ComplaintInfo selectByName(@Param("userName") String userName,@Param("orderNumber") String orderNumber);

    void update(HashMap<String, Object> map);
}

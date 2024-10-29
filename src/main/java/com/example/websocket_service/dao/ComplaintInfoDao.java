package com.example.websocket_service.dao;

import cn.hutool.core.date.DateTime;
import com.example.websocket_service.entity.ComplaintInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Mapper
@Repository
public interface ComplaintInfoDao {

    Boolean insert(ComplaintInfo saveFile);

    ComplaintInfo selectByName(@Param("userName") String userName, @Param("orderNumber") String orderNumber);

    String selectById(String orderNumber);

    void update(HashMap<String, Object> map);

    String selectByCreatedTime( @Param("userName") String complainant,@Param("dateTime") DateTime dateTime);
}

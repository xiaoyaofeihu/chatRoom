package com.example.websocket_service.dao;

import com.example.websocket_service.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FileInfoDao {
    List<FileInfo> selectByMd5(String md5);

    void save(FileInfo saveFile);

    void deletedFile(Integer id);

    void updateTime(@Param("id") Integer id,@Param("updateTime") String updateTime);

    FileInfo selectByUrl(String url);
}

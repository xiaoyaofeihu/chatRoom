package com.example.websocket_service.service;


import com.example.websocket_service.entity.FileInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FileService {
    void save(FileInfo saveFile);

    FileInfo selectByMd5(String md5);

    void deletedFile(Integer id);

    void updatedTime(@Param("id")Integer id,@Param("updateTime") String updateTime);

    List<FileInfo> getFileList();

    FileInfo selectByUrl(String content);
}

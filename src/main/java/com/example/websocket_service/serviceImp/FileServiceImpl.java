package com.example.websocket_service.serviceImp;

import com.example.websocket_service.dao.FileInfoDao;
import com.example.websocket_service.entity.FileInfo;
import com.example.websocket_service.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileInfoDao fileInfoDao;
    @Override
    public void save(FileInfo saveFile) {
        fileInfoDao.save(saveFile);
    }

    @Override
    public FileInfo selectByMd5(String md5) {
        List<FileInfo> fileInfoList = fileInfoDao.selectByMd5(md5);
        return fileInfoList.size() == 0 ? null : fileInfoList.get(0);
    }

    @Override
    public void deletedFile(Integer id) {
        fileInfoDao.deletedFile(id);
    }

    @Override
    public void updatedTime(Integer id, String updateTime) {
        fileInfoDao.updateTime(id, updateTime);
    }

    @Override
    public List<FileInfo> getFileList() {
        return fileInfoDao.selectByMd5(null);
    }

    @Override
    public FileInfo selectByUrl(String content) {
        return fileInfoDao.selectByUrl(content);
    }
}

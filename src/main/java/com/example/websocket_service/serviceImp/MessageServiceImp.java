package com.example.websocket_service.serviceImp;

import com.example.websocket_service.dao.FileInfoDao;
import com.example.websocket_service.dao.MessageDao;
import com.example.websocket_service.entity.FileInfo;
import com.example.websocket_service.entity.Message;
import com.example.websocket_service.entity.emuns.WebSocketEnum;
import com.example.websocket_service.entity.form.UserServiceF;
import com.example.websocket_service.entity.vo.MessageRecordV;
import com.example.websocket_service.entity.vo.PageBean;
import com.example.websocket_service.service.MessageService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageServiceImp implements MessageService {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private FileInfoDao fileInfoDao;

    @Override
    public void insert(Message message) {
        messageDao.insert(message);
    }

    @Override
    public List<Message> getMessages(String userId) {
        return messageDao.getMessages(userId);
    }

    @Override
    public PageBean<MessageRecordV> getServiceList(UserServiceF form) {
        PageHelper.startPage(form.getCurPage(), form.getPageSize(), true);
        List<MessageRecordV> list = new ArrayList<>();
        if (Objects.nonNull(form.getCustomerName()) && !WebSocketEnum.ADMIN.getCode().equals(form.getCustomerName())) {
            list = messageDao.getHistoryList(form.getUserName(), form.getCustomerName(), form.getCreatedTime());
        } else {
            list = messageDao.getServiceList(form);
        }
//        PageHelper.clearPage();
        PageBean<MessageRecordV> pageBean = new PageBean<>(list);
        List<MessageRecordV> recordVList = list.stream().filter(x -> (WebSocketEnum.CHATROOM.getCode().equals(x.getStatus()))).map(x -> {
            x.setVId(x.getId().toString());
//            if (2 == (x.getType())) {
//                List<String> picList = Arrays.asList(x.getContent().split(","));
//                FTPClient ftpClient = new FTPClient();
//                if (Objects.nonNull(picList)) {
//                    try {
//                        // 2、指定服务器地址（端口）
//                        ftpClient.connect(host, port);
//                        // 3、指定账号和密码
//                        ftpClient.login(username, password);
//                        int reply = ftpClient.getReplyCode();
//                        // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
//                        log.info("ftp连接成功: " + reply);
//                        if (!FTPReply.isPositiveCompletion(reply)) {
//                            ftpClient.disconnect();
//                            log.info("ftp连接失败");
//                        }
//                        // 转移到FTP服务器目录
//                        ftpClient.changeWorkingDirectory(remotePath);
//                        FileInputStream input;
//                        FTPFile[] fs = ftpClient.listFiles();
//                        for (FTPFile ff : fs) {
//                            if (picList.contains(ff.getName())) {
//                                File file = new File(ff.getName());
////                    if (file.exists()) {
////                        OutputStream is = new FileOutputStream(file);
////                        ftpClient.retrieveFile(x.getContent(), is);
////                        is.close();
////                        input = new FileInputStream(file);
////                        byte bytes[] = new byte[(int) file.length()];
////                        input.read(bytes);
////                        x.setPicData(bytes);
////                        input.close();
////                        ftpClient.logout();
////                    }
//                                //通过字节的方式写数据到文件中
//                                OutputStream is = new FileOutputStream(file);
//                                ftpClient.retrieveFile(ff.getName(), is);
//                                is.close();
//                                //通过字节的方式读取文件
//                                input = new FileInputStream(file);
//                                System.out.println(file.length());
//                                byte bytes[] = new byte[(int) file.length()];
//                                input.read(bytes);
//                                x.setPicData(bytes);
//                                input.close();
//                                file.delete();
//
//                            }
//                        }
//                        ftpClient.logout();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (ftpClient.isConnected()) {
//                            try {
//                                ftpClient.disconnect();
//                                log.info("ftpClient.isConnected()+ftp连接finally");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//            }
            if (1 == x.getType()) {
                FileInfo fileInfo = fileInfoDao.selectByUrl(x.getContent());
                if (Objects.nonNull(fileInfo)) {
                    x.setFileName(fileInfo.getName());
                    x.setFileSize(fileInfo.getSize());
                }
            }
            return x;
        }).collect(Collectors.toList());
//        return recordVList;
        pageBean.setList(recordVList);
        return pageBean;
    }

    @Override
    public void updateUrl(String userId, String url) {
        messageDao.updateUrl(userId, url);
    }

    @Override
    public void delete(Long id) {
        messageDao.delete(id);
    }
}

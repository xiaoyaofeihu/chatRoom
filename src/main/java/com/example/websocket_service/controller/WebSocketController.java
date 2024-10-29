package com.example.websocket_service.controller;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.websocket_service.config.SnowflakeIdWorker;
import com.example.websocket_service.entity.ComplaintInfo;
import com.example.websocket_service.entity.FileInfo;
import com.example.websocket_service.entity.Message;
import com.example.websocket_service.entity.emuns.TypeEnum;
import com.example.websocket_service.entity.emuns.WebSocketEnum;
import com.example.websocket_service.entity.vo.MessageChatInfoV;
import com.example.websocket_service.service.ComplaintService;
import com.example.websocket_service.service.FileService;
import com.example.websocket_service.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.websocket_service.config.FtpUtil.*;
import static com.example.websocket_service.entity.emuns.TypeEnum.MESSAGEFILETYPE;
import static java.lang.String.valueOf;

@Slf4j
@ServerEndpoint(value = "/websocket/{info}")
@Component

public class WebSocketController {


    private static MessageService messageService;

    private static FileService fileService;

    private static ComplaintService complaintService;


    //    private RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");    //获取redis实例
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        WebSocketController.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        WebSocketController.messageService = messageService;
    }

    @Autowired
    public void setFileService(FileService fileService) {
        WebSocketController.fileService = fileService;
    }

    @Autowired
    public void setComplaintService(ComplaintService complaintService) {
        WebSocketController.complaintService = complaintService;
    }


    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //使用ConcurrentHashMap是为了保证线程安全，HashMap在多线程的情况下会出现问题
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketController>> roomList = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话，需要通过他来给客户端发送消息

    private Session session;

    //重新加入房间标识（已加入时修改为1）

    private static int rejoin = 0;

//    private int waitCustomer = 0;
//
//    private int waitTime = 0;

    //等待人数
    private static AtomicInteger waitCustomer = new AtomicInteger();
    //等待時間
    private static AtomicInteger waitTime = new AtomicInteger();

    //info 格式： 标识,房间名,用户名,用户类型（保证唯一性）
    //flag 分类：exitRoom 离开 joinRoom 加入房间 chatRoom 聊天 createRoom 创建房间
    //入参：info：{flag分类，房间id，用户类型，客户姓名}

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketController>> getWebSocketMap() {
        return roomList;
    }

    @OnOpen
    public void onOpen(@PathParam("info") String param, Session session) throws Exception {
        this.session = session;
        String flag = param.split("[,]")[0];
        String roomId = param.split("[,]")[1];
        Integer userType = Integer.valueOf(param.split("[,]")[2]); //userType为1表示客户，为2表示客服
        String userName = param.split("[,]")[3];
        if (WebSocketEnum.JOINROOM.getCode().equals(flag) && TypeEnum.CUSTOMERTYPE.getCode().equals(userType)) {
            String orderNumber = param.split("[,]")[4];
            //保障一个房间中只有一个用户
            if (roomList.containsKey(roomId)) {
                Throwable throwable = new Throwable("该房间已存在一位用户");
                onError(roomId, throwable, session);
                return;
            }

//            if (TypeEnum.CUSTOMERTYPE.getCode().equals(redisUserType)){
//                Throwable throwable = new Throwable("该房间已存在一位用户");
//                onError(roomId,throwable,session);
//                return;
//            }
            //1、客户创建房间
            this.joinRoom(roomId, userName, userType);
            //2、查询客户的聊天记录
//            List<Message> messageList = messageService.getMessages(userName);
            String key = "wait_" + roomId + "_" + userName;
//            String key = "wait_" + roomId+"_"+userName+"_"+userType;
            Date date = new DateTime();
            //3、在redis中存储现在等待的用户数和等待时间
//            redisTemplate.opsForValue().set(key, date, 60, TimeUnit.MINUTES);
//            redisTemplate.opsForSet().add(key,date);
            redisTemplate.opsForZSet().add(key, userName, date.getTime());
            redisTemplate.expire(key, 4, TimeUnit.HOURS);
            //获取redis中所有的key，并设置等待时间
            Set<String> keys = redisTemplate.keys("wait_" + "*");
            List<String> values = redisTemplate.opsForValue().multiGet(keys);
//            waitCustomer = values.size();
            waitCustomer.set(values.size());
            //设置等待时间
//            waitTime = waitCustomer * 3;
            waitTime.set(waitCustomer.get() * 3);
            //4、返回聊天记录、用户数和房间号
            Map<String, Object> customerMap = new HashMap(100);
            customerMap.put("roomId", roomId);
            customerMap.put("userName", userName);
            customerMap.put("waitCustomer", waitCustomer);
            customerMap.put("waitTime", waitTime);
//            MessageInfoV infoV = new MessageInfoV();
//            infoV.setWaitCustomer(waitCustomer.intValue());
//            infoV.setWaitTime(waitTime.intValue());
//            infoV.setRoomId(roomId);
//            infoV.setUserName(userName);
            // 消息发送
//            sendMessage(infoV,session);
            sendMessage(customerMap, session);
            if (Objects.nonNull(redisTemplate.opsForValue().get(userName))) {
                redisTemplate.delete(userName);
            }
            redisTemplate.opsForValue().set(userName, orderNumber);
//            System.out.println(DateUtils.addHours(new Date(),1)+" redisTemplate.expireAt");
            redisTemplate.expire(userName, 4, TimeUnit.HOURS);
            System.out.println(redisTemplate.getExpire(userName) + "redisTemplate.getExpire");

        } else {
            //5、客服根据房间号加入房间
            this.joinRoom(roomId, userName, userType);
            ConcurrentHashMap<String, WebSocketController> hashMap = roomList.get(roomId);
            if (Objects.isNull(hashMap)) {
                throw new IOException("房间号为空！");
            } else {
                if (hashMap.size() > 2) {
                    Throwable throwable = new Throwable("该房间已存在一位客服");
                    onError(roomId, throwable, session);
                    return;
                }
            }
            String createdUser = param.split("[,]")[4];
            String userId = param.split("[,]")[5];
            //6、更新redis数据（等待用户数）
            String key = "wait_" + roomId + "_" + createdUser;
            redisTemplate.delete(key);
            waitMethod();
            String orderNumber = (String) redisTemplate.opsForValue().get(createdUser);
            System.out.println("orderNumber=" + orderNumber);
            ComplaintInfo info = complaintService.selectByName(createdUser, orderNumber);
            if (Objects.nonNull(info)) {
                HashMap<String, Object> map = new HashMap<>();
                if (Objects.isNull(info.getProcessingTime())) {
                    map.put("processingTime", new DateTime().toString());
                }
                map.put("editor", userName);
                map.put("editorId",userId);
                map.put("acceptor", userName);
                map.put("state", 1);
                map.put("orderNumber", orderNumber);
                map.put("complainant", createdUser);
//                info.setEditor(userName);
//                info.setProcessingTime(new DateTime().toString());
//                info.setAcceptor(userName);
//                info.setState(1);
                complaintService.update(map);
                Map<String, String> resultMap = JSON.parseObject(JSON.toJSONString(info), new TypeReference<Map<String, String>>() {
                });
                resultMap.put("flag", "createRoom");
                sendMessage(resultMap, session);
//                if (Objects.nonNull(info.getPicture())){
//                    sendPic(info.getPicture(),roomId, createdUser);
//                }
            }
        }
//        ComplaintInfo info = complaintService.selectByName(userName, orderNumber);
//        if (Objects.nonNull(info)){
//            sendMessage(info,session);
//            if (Objects.nonNull(info.getPicture())){
//                sendPic(info.getPicture(),roomId, userName);
//            }
//        }
    }

    //加入房间
    public void joinRoom(String roomId, String userName, Integer userType) throws IOException {

//        if (room.get(userName) != null) {
//            this.rejoin = 1;
//        }

        if (!roomList.containsKey(roomId) && TypeEnum.CUSTOMERSERVICETYPE.getCode().equals(userType)) {
            return;
        }

        if (!roomList.containsKey(roomId) || TypeEnum.CUSTOMERTYPE.getCode().equals(userType)) {
            ConcurrentHashMap<String, WebSocketController> room = new ConcurrentHashMap<>();
            room.put(userName, this);
            roomList.put(roomId, room);
            Map<String, Object> map = new HashMap<>();
            map.put("flag", "createRoom");
            map.put("createdUser", userName);
            try {
                sendMessage(map, session);
            } catch (Exception e) {
                log.info("创建房间失败");
                e.printStackTrace();
            }
            log.info("欢迎" + userName + "创建房间" + roomId);
        } else {
            ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);

            room.put(userName, this);
            log.info("客服" + userName + "已加入房间" + roomId);
            for (String item : room.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("flag", "joinRoom");
                map.put("name", "客服" + userName);
                map.put("status", "上线");
                //发送消息（排除自己）
                if (!Objects.equals(item, userName)) {
                    log.info("客服"+userName+"已上线房间"+roomId);
                    room.get(item).sendMessage(map, room.get(item).session);
                }
//                room.get(item).sendMessage(map);
            }
//            if (room.size() >2) {
//                Throwable throwable = new Throwable("该房间已存在一位客服");
//                onError(roomId, throwable, session);
//                return;
//            }
        }

    }


    //    {"flag":"","roomId":"","userName":"","content":"","createdUser":""}
    //接收来自用户的消息
    @OnMessage(maxMessageSize = 10485760)
    public void onMessage(String message, Session session) throws IOException {
        if ("ping".equals(message)) {
            System.out.println("收到心跳" + message);
            try {
                session.getBasicRemote().sendText("pong");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //7、开始聊天
//        JSONObject param = new JSONObject(message);
//        String flag = (String) param.get("flag");
//        String createdUser = (String) param.get("createdUser");
//        String roomId = (String) param.get("roomId");
//        String userName = (String) param.get("userName");
//        String fileName = (String) param.get("fileName");
//        String url = (String) param.get("url");
        MessageChatInfoV param = JSONObject.parseObject(message, MessageChatInfoV.class);
        String userId = param.getUserId();
        String flag = param.getFlag();
        String roomId = param.getRoomId();
        String createdUser = param.getCreatedUser();
        String userName = param.getUserName();
        Integer userType = param.getUserType();
        Integer type = param.getType();
        String content = param.getContent();


        if (WebSocketEnum.EXITROOM.getCode().equals(flag)) {
            param.setDate(df.format(new Date()));
            ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);
            room.remove(userName);
            //判断房间中是否有人，如果没人则删除房间，否则通知其他用户该用户已经下线
            if (room.size() == 0) {
                roomList.remove(roomId);
            } else {
                for (String item : room.keySet()) {
                    param.setStatus("下线");
                    room.get(item).sendMessage(param, room.get(item).session);
                }
            }
            //如果是客户退出，需要更新redis
            if (TypeEnum.CUSTOMERTYPE.getCode().equals(param.getUserType())) {
                String key = "wait_" + roomId + "_" + createdUser;
//                String key = "wait_" + roomId+"_"+createdUser+"_"+userType;
                redisTemplate.delete(key);
            }
            session.close();
        } else if (WebSocketEnum.CHATROOM.getCode().equals(flag)) {
            param.setDate(df.format(new Date()));

            ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);

//            if (room.size()>1){
//                roomList.remove(roomId);
//                for (String exRoom : roomList.keySet()) {
//                    for (String value : roomList.get(exRoom).keySet()) {
//                        Map<String, Object> params = new HashMap<>();
//                        params.put("waitCustomer",111111);
//                        params.put("waitTime", 111111);
//                        roomList.get(exRoom).get(value).sendMessage(params);
//                    }
//                }
//            }
//            try {
//                sendPic(url,roomId);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //单独处理图片

//            if (MESSAGEPICTYPE.getCode().equals(type)) {
//                try {
////                    sendPic(content, roomId, userName);
//                    sendPic(content,roomId, userName);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            if (Objects.nonNull(room)) {
                if (Objects.nonNull(room.get(userName))) {
                    if (MESSAGEFILETYPE.getCode().equals(type)) {
                        FileInfo fileInfo = fileService.selectByUrl(content);
                        if (Objects.nonNull(fileInfo)) {
                            param.setFileName(fileInfo.getName());
                            param.setFileSize(fileInfo.getSize());
                        }
                    }
                    if (room.get(userName).rejoin == 0) {
                        //8、insert聊天记录
                        Message message1 = new Message();
                        BeanUtils.copyProperties(param, message1);
                        message1.setStatus(flag);
                        message1.setRoomId(roomId);
                        message1.setCreatedTime(new DateTime().toString());
                        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);
                        long id = idWorker.nextId();
                        message1.setId(id);
                        messageService.insert(message1);
                        System.out.println("id" + message1.getId());
                        for (String item : room.keySet()) {

                            if (!Objects.equals(item, userName)) {
                                room.get(item).sendMessage(param, room.get(item).session);
                            }
                        }
                    } else {
//                        room.get(userName).sendMessage(param, session);
                    }
                }
            }
        }
    }

//    private void ftpConn(String roomId, String userName, String content) throws IOException {
//        FTPClient ftpClient = new FTPClient();
//        // 2、指定服务器地址（端口）
//        ftpClient.connect(host, port);
//        // 3、指定账号和密码
//        ftpClient.login(username, password);
//        int reply = ftpClient.getReplyCode();
//        // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
//        log.info("ftp连接成功: "+reply);
//        if (!FTPReply.isPositiveCompletion(reply)) {
//            ftpClient.disconnect();
//            log.info("ftp连接失败");
//        }
//        // 转移到FTP服务器目录
//        ftpClient.changeWorkingDirectory(remotePath);
////                    sendPic(content,roomId,userName);
//        sendPic(content, roomId, userName);
//    }

    @OnClose
    public void onClose(@PathParam("info") String param, Session session) {
        if (Objects.nonNull(param)) {
            String roomId = param.split("[,]")[1];
            Integer userType = Integer.valueOf(param.split("[,]")[2]);
            String userName = param.split("[,]")[3];
            ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);
            if (Objects.nonNull(room)) {
                Map<String, Object> params = new HashMap<>();
                params.put("flag", "exitRoom");
                params.put("roomId", roomId);
                if (TypeEnum.CUSTOMERSERVICETYPE.getCode().equals(userType)) {
                    params.put("userName", "客服" + userName);
                } else {
                    params.put("userName", userName);
                    String key = "wait_" + roomId + "_" + userName;
                    redisTemplate.delete(key);
                }
                params.put("status", "下线");
                params.put("userType", userType);
                log.info(userName + "已下线");
                room.remove(userName);
//            String key = "wait_" + roomId+"_"+userName+"_"+userType;
//            redisTemplate.delete(roomId);
                for (String item : room.keySet()) {
                    room.get(item).sendMessage(params, room.get(item).session);
                }
                //特殊处理多客服情况
                if (room.size() >= 2) {
                    return;
                }

                if (TypeEnum.CUSTOMERSERVICETYPE.getCode().equals(userType) && !CollectionUtils.isEmpty(room.keySet())) {
                    String createdUser = param.split("[,]")[4];
                    String userId = param.split("[,]")[5];
                    String orderNumber = (String) redisTemplate.opsForValue().get(createdUser);
                    ComplaintInfo info = complaintService.selectByName(createdUser, orderNumber);
                    if (Objects.nonNull(info)) {
                        HashMap<String, Object> map = new HashMap<>();
//                    info.setState(2);
//                    info.setEditorDate(new DateTime().toString());
                        map.put("editor", userName);
                        map.put("editorId",userId);
                        map.put("editorDate", new DateTime().toString());
                        map.put("acceptor", userName);
                        map.put("state", 2);
                        map.put("orderNumber", orderNumber);
                        map.put("complainant", createdUser);
                        complaintService.update(map);
                    }
                    if (room.containsKey(createdUser)) {
                        String key = "wait_" + roomId + "_" + createdUser;
                        redisTemplate.opsForZSet().add(key, createdUser, new DateTime().getTime());
                        redisTemplate.expire(key, 4, TimeUnit.HOURS);
                        waitMethod();
                    }
                }
                redisTemplate.delete(userName);
                if (room.size() == 0) {
                    roomList.remove(roomId);
                }
            }
        }
    }

    @OnError
    public void onError(@PathParam("info") String param, Throwable t, Session session) throws Exception {

        log.error("[WebSocketServer] Connection Exception : info = " + param + " , throwable = " + t.getMessage() + "---" + t);
        log.error("session = " + session.toString());
        synchronized (session) {
            HashMap map = new HashMap<>();
            map.put("flag", "closed");
            String jsonString = JSONObject.toJSONString(map);
            if (session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(jsonString);
                }
            }
        }
        session.close();
    }

    //    //发送消息
//    public void sendMessage(Map<String, Object> map,Session session){
//        String jsonObject = JSON.toJSONString(map);
//        //getAsyncRemote是非阻塞式的
////        this.session.getAsyncRemote().sendText(jsonObject);
//        synchronized(session){
//
//            session.getAsyncRemote().sendText(jsonObject);
//
//        }
//    }
    //发送消息(map格式)
    public void sendMessage(Object map, Session session) {
        String jsonObject = JSONObject.toJSONString(map);
        //getAsyncRemote是非阻塞式的
        //        this.session.getAsyncRemote().sendText(jsonObject);
        synchronized (session) {

            session.getAsyncRemote().sendText(jsonObject);

        }
    }


    //更新等待人数和等待时间的方法
    private void waitMethod() {
        Set<String> keys = redisTemplate.keys("wait_" + "*");
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        for (String exRoom : keys) {
            exRoom = exRoom.split("_")[1];
            if (roomList.containsKey(exRoom)) {
                for (String value : roomList.get(exRoom).keySet()) {
                    Map<String, Object> params1 = new HashMap<>();
                    params1.put("waitCustomer", values.size());
                    params1.put("waitTime", values.size() * 3);
                    roomList.get(exRoom).get(value).sendMessage(params1, roomList.get(exRoom).get(value).session);
                }
            }
        }
    }

    //    public void sendPicture(String fileName){
//        FileInputStream input;
//        try {
//            File file=new File("D:\\"+fileName);
//            input = new FileInputStream(file);
//            byte bytes[] = new byte[(int) file.length()];
//            input.read(bytes);
//            BinaryMessage byteMessage=new BinaryMessage(bytes);
//            session.sendFileMessage(byteMessage);
//            input.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public void  sendPic(String fileName,String roomId,String userName) throws Exception {
//        log.info("【图片】推送文件名称:{}", fileName);
//        ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);
//        for (String item : room.keySet()) {
//            FileInputStream input;
//            try {
//                //不同sid，获取的文件不一样,目前只区分：1和其他
////                String filePath="D:\\data\\images\\2022\\01\\13\\";
////                filePath=filePath+fileName+".jpg";
////                log.debug("【手动执行图片切换】图片路径：{}",filePath);
////                String filePath = "D:\\files\\6b479b567a854c0aa67e6101c445b438.webp";
//                //读取文件
////                File file=new File(filePath);
//                File file=new File(fileName);
//                //生成字节数组
////                if(file==null || !file.exists()){
////                    log.debug("【手动执行图片切换】文件不存在，不执行消息推送");
////                    return;
////                }
//
//                input = new FileInputStream(file);
//                byte bytes[] = new byte[(int) file.length()];
//                input.read(bytes);
//                //转为ByteBuffer
////                BinaryMessage byteMessage=new BinaryMessage(bytes);
//                if (!Objects.equals(item,userName)){
//                    room.get(item).sendFileMessage(ByteBuffer.wrap(bytes));
//                }
//
//                input.close();
//            } catch (IOException e) {
//                log.error("消息发送失败" + e.getMessage(), e);
//                return;
//            }
//        }
//        log.debug("【图片】推送成功");
//        return;
//    }

    public void sendPic(String fileName, String roomId, String userName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        // 2、指定服务器地址（端口）
        ftpClient.connect(host, port);
        // 3、指定账号和密码
        ftpClient.login(username, password);
        int reply = ftpClient.getReplyCode();
        // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
        log.info("ftp连接成功: " + reply);
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            log.info("ftp连接失败");
        }
        // 转移到FTP服务器目录
        ftpClient.changeWorkingDirectory(remotePath);
//        List<String> picList = Arrays.asList(fileName.split(","));
        log.info("【图片】推送文件名称:{}", fileName);
        ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);
        if (Objects.nonNull(room)) {
            for (String item : room.keySet()) {
                FileInputStream input;
                try {
                    //不同sid，获取的文件不一样,目前只区分：1和其他
//                String filePath="D:\\data\\images\\2022\\01\\13\\";
//                filePath=filePath+fileName+".jpg";
//                log.debug("【手动执行图片切换】图片路径：{}",filePath);
//                String filePath = "D:\\files\\6b479b567a854c0aa67e6101c445b438.webp";
                    //读取文件
//                File file=new File(filePath);
                    FTPFile[] fs = ftpClient.listFiles();
                    for (FTPFile ff : fs) {
//                            if (picList.contains(ff.getName())) {
                        if (Objects.nonNull(fileName)) {
                            if (fileName.equals(ff.getName())) {
                                File file = new File(ff.getName());
//                            if (!file.getParentFile().exists()) {
//                                file.getParentFile().mkdirs();
//                            }
//                            if (!file.exists()) {
//                                file.createNewFile();
//                            }
                                //生成字节数组
//                if(file==null || !file.exists()){
//                    log.debug("【手动执行图片切换】文件不存在，不执行消息推送");
//                    return;
//                }
                                OutputStream is = new FileOutputStream(file);
                                //从服务器检索命名文件并将其写入给定的OutputStream
                                ftpClient.retrieveFile(ff.getName(), is);
                                is.close();
                                input = new FileInputStream(file);
                                int available = input.available();
                                System.out.println(available + "======================");
                                byte bytes[] = new byte[(int) file.length()];
                                input.read(bytes);
                                int available1 = input.available();
                                System.out.println("-------------------------" + available1);
                                //转为ByteBuffer
//                BinaryMessage byteMessage=new BinaryMessage(bytes);
                                if (!Objects.equals(item, userName)) {
                                    room.get(item).sendFileMessage(ByteBuffer.wrap(bytes));
                                }
                                input.close();
                                file.delete();
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("消息发送失败" + e.getMessage(), e);
                    return;
                }
            }
        }
        ftpClient.logout();
        log.debug("【图片】推送成功");
        return;
    }

//    /**
//     * 主动推送到客户端，发文件流消息
//     *
//     * @param fileName 文件名称
//     */
//    public void sendPic(String fileName,String roomId) throws Exception {
//        log.info("【手动执行图片切换】推送消息到窗口,sid：{}，推送文件名称:{}", fileName);
//        ConcurrentHashMap<String, WebSocketController> room = roomList.get(roomId);
//        for (String item : room.keySet()) {
//            try {
//                //不同sid，获取的文件不一样,目前只区分：1和其他
////                String filePath="D:\\data\\images\\2022\\01\\13\\";
////                filePath=filePath+fileName+".jpg";
////                log.debug("【手动执行图片切换】图片路径：{}",filePath);
//                String filePath = "D:\\files\\cf726969bfab4efd8beffa04044ebe7c.webp";
//                //读取文件
//                File file=new File(filePath);
//                //生成字节数组
////                if(file==null || !file.exists()){
////                    log.debug("【手动执行图片切换】文件不存在，不执行消息推送");
////                    return;
////                }
//
//                byte[] bytes=fileToByte(file);
//                //转为ByteBuffer
//                room.get(item).sendFileMessage(ByteBuffer.wrap(bytes));
//            } catch (IOException e) {
//                log.error("消息发送失败" + e.getMessage(), e);
//                return;
//            }
//        }
//        log.debug("【手动执行图片切换】推送成功");
//        return;
//    }

    public static byte[] fileToByte(File img) throws Exception {
        byte[] bytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage bi;
            bi = ImageIO.read(img);
            ImageIO.write(bi, "jpg", baos);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baos.close();
        }
        return bytes;
    }

    /**
     * 服务器主动提推送消息
     *
     * @param fileByteBuffer 文件流内容
     * @throws IOException io异常抛出
     */
    public void sendFileMessage(ByteBuffer fileByteBuffer) throws IOException {
        log.debug("【手动执行图片切换】推送fileByteBuffer:{}", fileByteBuffer);
        this.session.getBasicRemote().sendBinary(fileByteBuffer);
    }

    @Scheduled(cron = "0 */1 * * * ?")
//    @Scheduled(fixedRate=66000)
    public void redisTask() throws Exception {
//            String key = "wait_" + roomId+"_"+userName+"_"+userType;
        //3、在redis中存储现在等待的用户数和等待时间
//            redisTemplate.opsForValue().set(key, date, 60, TimeUnit.MINUTES);
//            redisTemplate.opsForSet().add(key,date);
        ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketController>> map = WebSocketController.getWebSocketMap();
        Set<String> keys = redisTemplate.keys("wait_" + "*");
        for (String key : keys) {
            String userName = key.split("_")[2];
            String roomId = key.split("_")[1];
            ConcurrentHashMap<String, WebSocketController> map1 = map.get(roomId);
            Double score = redisTemplate.opsForZSet().score(key, userName);
            if (Objects.nonNull(score)) {
                Date date = new Date(score.longValue());
                Date addHours = DateUtils.addHours(date, 3);
                log.info(roomId + userName);
                log.info("addHours" + addHours.toString() + "--" + new Date().toString() + "--" + "new Date().after(addHours)" + valueOf(new Date().after(addHours)));
                if (new Date().after(addHours) || new Date().equals(addHours)) {
                    Throwable throwable = new Throwable("超过三小时，请重新登录！");
                    onError(roomId, throwable, map1.get(userName).session);
                }
            }

        }

    }
}

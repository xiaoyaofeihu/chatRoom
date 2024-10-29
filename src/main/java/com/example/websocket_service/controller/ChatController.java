package com.example.websocket_service.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.example.websocket_service.config.Base64StrToImage;
import com.example.websocket_service.config.FtpUtil;
import com.example.websocket_service.config.RestResponse;
import com.example.websocket_service.entity.BASE64DecodedMultipartFile;
import com.example.websocket_service.entity.FileInfo;
import com.example.websocket_service.entity.Message;
import com.example.websocket_service.entity.form.ChatInfoF;
import com.example.websocket_service.service.FileService;
import com.example.websocket_service.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private FileService fileService;

    @Autowired
    private MessageService messageService;

    @Value("${files.upload.path}")  // spel表达式
    private String fileUploadPath;

    /**
     * 默认的文件名最大长度 10
     */
    public static final int FILE_MAX_SIZE = 10;

    /**
     * 上传聊天图片
     * **/
//    @PostMapping(value = "/upimg")
    @ResponseBody
    public JSONObject upauz(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        JSONObject res = new JSONObject();
        JSONObject resUrl = new JSONObject();
        LocalDate today = LocalDate.now();
        Instant timestamp = Instant.now();
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String filenames = today + String.valueOf(timestamp.toEpochMilli()) + "."+ext;
        file.transferTo(new File("D:\\" + filenames));
        resUrl.put("url", "/pic/" + filenames);
        res.put("msg", "");
        res.put("code", 0);
        res.put("data", resUrl);
        return res;
    }




    // 图片上传
    @PostMapping("/uploadImg")
    public RestResponse uploadImg(@RequestBody ChatInfoF form) throws IOException {
        BASE64DecodedMultipartFile base64DecodedMultipartFile = null;
        if (Objects.nonNull(form.getPicture())) {
            base64DecodedMultipartFile = (BASE64DecodedMultipartFile) Base64StrToImage.base64MutipartFile(form.getPicture());
            if (Objects.nonNull(base64DecodedMultipartFile)) {
                form.setFile(base64DecodedMultipartFile);
            }
        }
        return getRestResponse(form.getUserId(), form.getFile());
    }

    //文件上传
    @PostMapping("/uploadFile")
    public RestResponse uploadFile(@RequestParam(value = "userId",required = false) String userId,@RequestParam(value = "fileName",required = false) MultipartFile fileName) throws IOException {
        return getRestResponse(userId, fileName);
    }

    private RestResponse getRestResponse(String userId, MultipartFile file) throws IOException {
        if (Objects.isNull(file)) {
            if (file.isEmpty()) {
                return new RestResponse().error("文件为空");
            }
            return new RestResponse().error("file is null");
        }

        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename); // 得到文件的扩展名
        long size = file.getSize()/1024/1024;
        System.out.println(size);
        if (size>FILE_MAX_SIZE){
            return new RestResponse().error("文件太大，超过10MB");
        }
        // 定义一个文件唯一的标识码
//        String uuid = IdUtil.fastSimpleUUID();
//        String fileUUID = uuid + StrUtil.DOT + type;
//
//        File uploadFile = new File(fileUploadPath + fileUUID);
//        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
//        File parentFile = uploadFile.getParentFile();
//        if(!parentFile.exists()) {
//            parentFile.mkdirs();
//        }
        String url;
        // 获取文件的md5 ，同一个文件MD5相同
        String md5 = SecureUtil.md5(file.getInputStream());
        String parser = getMimeTypeByParser(file.getInputStream());
        // 从数据库查询是否存在相同的记录
        FileInfo dbFileInfo = getFileByMd5(md5);
        if (dbFileInfo != null) { // 文件已存在
            url = dbFileInfo.getUrl();
            //更新時間
            fileService.updatedTime(dbFileInfo.getId(),new DateTime().toString());
            return new RestResponse().putApp(url);
        }
        url = FtpUtil.uploadFile(file,false);
        if(Objects.isNull(url)){
            return new RestResponse().error("连接ftp服务器失败，url为空");
        }
        // 上传文件到磁盘
//        file.transferTo(uploadFile);
//        // 数据库若不存在重复文件，则不删除刚才上传的文件
////            url = "http://localhost:9090/file/" + fileUUID;
//        url = uploadFile.getAbsolutePath();
////        urlStr = url.replaceAll("\\\\","\\\\\\\\");
        // 存储数据库
        FileInfo saveFile = new FileInfo();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
//        saveFile.setSize(size/1024); // kb
        saveFile.setSize(file.getSize()); // kb
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        saveFile.setUserId(userId);
        saveFile.setIsDelete(0);
        saveFile.setCreatedTime(new DateTime().toString());
        saveFile.setUpdatedTime(new DateTime().toString());
        fileService.save(saveFile);
        return new RestResponse().putApp(url);
    }

//    // 多文件上传
//    @PostMapping("/upload")
//    public RestResponse upload(@RequestParam(value = "file") List<MultipartFile> fileList, @RequestParam(value = "userId") String userId) throws IOException {
//        if (CollectionUtils.isEmpty(fileList)) {
//            return new RestResponse().error("文件为空");
//        }
//        StringBuilder build = new StringBuilder();
//        for (MultipartFile file : fileList) {
//            String originalFilename = file.getOriginalFilename();
//            String type = FileUtil.extName(originalFilename); // 得到文件的扩展名
//            long size = file.getSize() / 1024 / 1024;
//            System.out.println(size);
//            if (size > FILE_MAX_SIZE) {
//                return new RestResponse().error("文件太大，超过10MB");
//            }
//            // 定义一个文件唯一的标识码
////        String uuid = IdUtil.fastSimpleUUID();
////        String fileUUID = uuid + StrUtil.DOT + type;
////
////        File uploadFile = new File(fileUploadPath + fileUUID);
////        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
////        File parentFile = uploadFile.getParentFile();
////        if(!parentFile.exists()) {
////            parentFile.mkdirs();
////        }
//            // 获取文件的md5 ，同一个文件MD5相同
//            String md5 = SecureUtil.md5(file.getInputStream());
//            String parser = getMimeTypeByParser(file.getInputStream());
//            String url;
//            // 从数据库查询是否存在相同的记录
//            FileInfo dbFileInfo = getFileByMd5(md5);
//            if (dbFileInfo != null) { // 文件已存在
//                url = dbFileInfo.getUrl();
//                //更新時間
//                fileService.updatedTime(dbFileInfo.getId(), new DateTime().toString());
//                build.append(url+",");
//            } else {
//                url = FtpUtil.uploadFile(file);
//                build.append(url+",");
//                if (Objects.isNull(url)) {
//                    return new RestResponse().error("连接ftp服务器失败，url为空");
//                }
//            }
//            // 上传文件到磁盘
////        file.transferTo(uploadFile);
////        // 数据库若不存在重复文件，则不删除刚才上传的文件
//////            url = "http://localhost:9090/file/" + fileUUID;
////        url = uploadFile.getAbsolutePath();
//////        urlStr = url.replaceAll("\\\\","\\\\\\\\");
//            // 存储数据库
//            FileInfo saveFile = new FileInfo();
//            saveFile.setName(originalFilename);
//            saveFile.setType(type);
////        saveFile.setSize(size/1024); // kb
//            saveFile.setSize(file.getSize()); // kb
//            saveFile.setUrl(url);
//            saveFile.setMd5(md5);
//            saveFile.setUserId(userId);
//            saveFile.setIsDelete(0);
//            saveFile.setCreatedTime(new DateTime().toString());
//            saveFile.setUpdatedTime(new DateTime().toString());
//            fileService.save(saveFile);
//        }
//        build.deleteCharAt(build.length()-1);
//        return new RestResponse().put(build.toString());
//    }

    // 通过MD5查询文件
    private FileInfo getFileByMd5(String md5) {
        // 查询文件的md5是否存在
        return fileService.selectByMd5(md5);
    }

    public static String getMimeTypeByParser(InputStream stream) {
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler();
        try {
            parser.parse(stream, handler, metadata);
        } catch (IOException | org.xml.sax.SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }

        return metadata.get(HttpHeaders.CONTENT_TYPE);
    }

    // 文件下载接口
    @GetMapping("/download")
    public void download(@RequestParam("fileUUID") String fileUUID,String orderNumber,
                          HttpServletResponse response) throws Exception {
//        // 根据文件的唯一标识码获取文件
//        File uploadFile = new File(fileUUID);
//        boolean exists = uploadFile.exists();
//        if (exists) {
//            // 设置输出流的格式
//            ServletOutputStream os = response.getOutputStream();
//            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
//            response.setContentType("application/octet-stream");
//
//            // 读取文件的字节流
//            os.write(FileUtil.readBytes(uploadFile));
//            os.flush();
//            os.close();
//        }
        FtpUtil.downloadFile(fileUUID, orderNumber, response);

//        if (!result){
//            throw new Exception("文件下载失败！");
//        }
//        return result;
    }

    @GetMapping("/deleteFile")
    public boolean deleteFile(@RequestParam("fileUUID") String fileUUID,
                         HttpServletResponse response) throws IOException {
        return FtpUtil.delFile(fileUUID);
    }

//    @GetMapping("/pic")
//    public void pic(@RequestParam MultipartFile file) throws Exception {
//        fileToByte(new File(file.getOriginalFilename()));
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

    //定时任务进行删除(每天24点执行，以7天为周期)
//    @Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")
    public void fileTask() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<FileInfo> fileList = fileService.getFileList();
        log.info("信息列表:{}",fileList);
        for (FileInfo info : fileList) {
            Integer fileId = info.getId();
            try {
                Date parse = sdf.parse(info.getUpdatedTime());
                Date afterDay = getAfterDay(parse,7);
                if (Objects.nonNull(afterDay)) {
//                if (sdf.format(new Date()).equals(sdf.format(afterDay) )) {
                    if (afterDay.before(new Date())) {
                        //删除文件
//                    File file = new File(info.getUrl());
                        boolean delete = FtpUtil.delFile(info.getUrl());
                        if (delete) {
                            //更新数据库
                            fileService.deletedFile(fileId);
                            messageService.updateUrl(info.getUserId(), info.getUrl());
                            log.info("开始删除文件数据：{}", fileId);
                        }
                    }
                }
                log.info("定时任务执行完毕：{}",fileId);
            } catch (ParseException e) {
                log.error("定时任务报错：{}",fileId);
                e.printStackTrace();
            }
        }

        //清除message表
        List<Message> messageList = messageService.getMessages(null);
        for (Message message : messageList) {
            try {
                Date parse = sdf.parse(message.getCreatedTime());
                Date afterDay = DateUtils.addDays(parse,7);
                if (Objects.nonNull(afterDay)){
                    if (afterDay.before(new Date())){
                        messageService.delete(message.getId());
                        log.info("开始删除消息数据：{}",message.getId());
                    }
                    log.info("消息删除执行完毕：{}",message.getId());
                }

            } catch (ParseException e) {
                log.info("消息删除执行报错：{}",message.getId());
                e.printStackTrace();
            }
        }
        deleteFile(new File(fileUploadPath));

    }

    private static Date getAfterDay(Date date, int diff) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date == null ? new Date() : date);
        calendar.add(Calendar.DATE, diff);
        return calendar.getTime();
    }

    public static Boolean deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            System.out.println("文件删除失败,请检查文件是否存在以及文件路径是否正确");
            return false;
        }
        //获取目录下子文件
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f : files) {
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()) {
                //递归删除目录下的文件
                deleteFile(f);
            } else {
                //文件删除
                f.delete();
                //打印文件名
                System.out.println("文件名：" + f.getName());
            }
        }
        //文件夹删除
        file.delete();
        System.out.println("目录名：" + file.getName());
        return true;
    }
}




package com.example.websocket_service.config;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class FtpUtil {
    // ftp服务器地址
    public static final String host = "192.168.1.132";
    // ftp服务器端口
    public static final int port = 21;
    // ftp登录账号
    public static final String username = "";
    // ftp登录密码
    public static final String password = "";
    // ftp服务器上的相对路径
    public static final String remotePath = "/ftp/";
    public static final String complaintPath = "/complaint/";

    /** 本地字符编码 */
    private static String LOCAL_CHARSET = "GBK";

    // FTP协议里面，规定文件名编码为iso-8859-1
    private static String SERVER_CHARSET = "ISO-8859-1";

    private  static  org.slf4j.Logger log = LoggerFactory.getLogger(FtpUtil.class);

    @Value("${files.upload.path}")  // spel表达式
    private static String fileUploadPath;
 
    /**
     * 向ftp服务器上传文件
     *
     * @param file
     * @return
     */
    public static String uploadFile(MultipartFile file,Boolean ret) {
        // 1、创建FTPClient对象
        FTPClient ftpClient = new FTPClient();
        // 保存FTP控制连接使用的字符集，必须在连接前设置
        ftpClient.setControlEncoding("UTF-8");
        String remote = null;
        try {
            // 2、指定服务器地址（端口）
            ftpClient.connect(host, port);
            // 3、指定账号和密码
            ftpClient.login(username, password);
            // 连接成功或者失败返回的状态码
            int reply = ftpClient.getReplyCode();
            // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
            log.info("ftp连接成功: "+reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return null;
            }
            // 创建目录
            if (ret) {
                String basePath = "/";
                for (String p : complaintPath.split("/")) {
                    basePath += (p + "/");
                    // 判断目录是否已经存在
                    boolean hasPath = ftpClient.changeWorkingDirectory(basePath);
                    if (!hasPath) {
                        // 创建目录 一次只能创建一个目录
                        ftpClient.makeDirectory(basePath);
                    }
                }
                // 重新指定上传的路径
                ftpClient.changeWorkingDirectory(complaintPath);
            } else {
                String basePath = "/";
                for (String p : remotePath.split("/")) {
                    basePath += (p + "/");
                    // 判断目录是否已经存在
                    boolean hasPath = ftpClient.changeWorkingDirectory(basePath);
                    if (!hasPath) {
                        // 创建目录 一次只能创建一个目录
                        ftpClient.makeDirectory(basePath);
                    }
                }
                ftpClient.changeWorkingDirectory(remotePath);
            }
            // 6、指定上传方式为二进制方式
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 得到文件后缀
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            remote = IdUtil.fastSimpleUUID() + suffix;
            // 7、remote指定上传远程服务器的文件名 local指本地的输入流
            boolean result = ftpClient.storeFile(new String(remote.getBytes("UTF-8"),"iso-8859-1"), file.getInputStream());
            if(!result){
                log.error("ftpClient.storeFile() 文件上传失败！");
                return null;
            }
            System.out.println(result+"==============================");
        } catch (SocketException e) {
            log.error("文件上传连接错误：" + e.getMessage());
        } catch (IOException e) {
            log.error("文件上传失败：" + e.getMessage());
        } finally {
            try {
                if (file.getInputStream() != null)
                    file.getInputStream().close();
                ftpClient.logout();
                if (ftpClient.isConnected())
                    ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return remote;
    }

    /**
     * 从ftp服务器下载文件
     *
     * @param fileName  要下载的文件名
     * @param localPath 下载后保存到本地的路径
     * @param response
     * @return
     */
    public static void downloadFile(String fileName, String orderNumber, HttpServletResponse response) throws Exception {
        log.info("filename"+fileName+"======="+"orderNumber"+orderNumber);
        boolean flag = false;
        // 1、创建FTPClient对象
        FTPClient ftpClient = new FTPClient();
        try {
            FTPFile[] fs = new FTPFile[]{};
            // 2、指定服务器地址（端口）
            ftpClient.connect(host, port);
            // 3、指定账号和密码
            ftpClient.login(username, password);
            // 连接成功或者失败返回的状态码
            int reply = ftpClient.getReplyCode();
            // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
            log.info("ftp连接成功: "+reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.info("ftp连接失败");
                throw new Exception("文件下载失败！");
            }
            // 转移到FTP服务器目录
            if (Objects.nonNull(orderNumber)){
                ftpClient.changeWorkingDirectory(complaintPath);
                fs = ftpClient.listFiles(complaintPath);
            }else{
                ftpClient.changeWorkingDirectory(remotePath);
                fs = ftpClient.listFiles(remotePath);
            }
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
//                    String name = new String(ff.getName().getBytes(SERVER_CHARSET),LOCAL_CHARSET);
//                    File localFile = new File(localPath + "/" + ff.getName());
                    File localFile = new File("D:\\files2\\"+ff.getName());
                    File parent = localFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    //通过字节的方式写数据到文件中
                    OutputStream is = new FileOutputStream(localFile);
                    //从服务器检索命名文件并将其写入给定的OutputStream
                    ftpClient.retrieveFile(ff.getName(), is);
                    is.flush();
                    is.close();
                    //清空response
                    response.reset();
                    ServletOutputStream os = response.getOutputStream();
                    response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                    response.setContentType("application/octet-stream");

                    // 读取文件的字节流
                    os.write(FileUtil.readBytes(localFile));
                    System.out.println("os"+FileUtil.readBytes(localFile).length);
                    os.flush();
                    os.close();

//                    localFile.delete();
                }
            }
//            delAllFile("/files2/");
            ftpClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                    log.info("ftpClient.isConnected()+ftp连接finally");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


//    //删除文件夹
////param folderPath 文件夹完整绝对路径
//    public static void delFolder(String folderPath) {
//        try {
//            delAllFile(folderPath); //删除完里面所有内容
//            String filePath = folderPath;
//            filePath = filePath.toString();
//            java.io.File myFilePath = new java.io.File(filePath);
//            myFilePath.delete(); //删除空文件夹
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //删除指定文件夹下所有文件
////param path 文件夹完整绝对路径
//    public static boolean delAllFile(String path) {
//        boolean flag = false;
//        File file = new File(path);
//        if (!file.exists()) {
//            return flag;
//        }
//        if (!file.isDirectory()) {
//            return flag;
//        }
//        String[] tempList = file.list();
//        File temp = null;
//        for (int i = 0; i < tempList.length; i++) {
//            if (path.endsWith(File.separator)) {
//                temp = new File(path + tempList[i]);
//            } else {
//                temp = new File(path + File.separator + tempList[i]);
//            }
//            if (temp.isFile()) {
//                temp.delete();
//            }
//            if (temp.isDirectory()) {
//                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
//                delFolder(path + "/" + tempList[i]);//再删除空文件夹
//                flag = true;
//            }
//        }
//        return flag;
//    }

 
    /**
     * 删除ftp服务器上的文件
     *
     * @param filePath  ftp服务器保存目录
     * @return 成功返回true，否则返回false
     */
    public static boolean delFile(String filePath){
        boolean flag = false;
        // 1、创建FTPClient对象
        FTPClient client = new FTPClient();
        try {
            // 2、指定服务器地址（端口）
            client.connect(host, port);
            // 3、指定账号和密码
            client.login(username, password);
            // 连接成功或者失败返回的状态码
            int reply = client.getReplyCode();
            // 如果reply返回230表示成功，如果返回530表示无密码或用户名错误或密码错误或用户权限问题。
            log.info("ftp连接成功: "+reply);
            if(!FTPReply.isPositiveCompletion(reply)){
                client.disconnect();
                log.info("ftp连接失败");
                return flag;
            }
            System.out.println("要删除的目录文件："+filePath);
            if(filePath.indexOf(".")<=0){
                return flag;
            }
            System.out.println(remotePath+filePath);
            flag = client.deleteFile(remotePath + filePath);
//            flag=client.deleteFile(new String(filePath.getBytes(LOCAL_CHARSET),SERVER_CHARSET));
            client.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(client.isConnected()){
                    client.disconnect();
                    return flag;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
}
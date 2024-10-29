package com.example.websocket_service.serviceImp;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.example.websocket_service.config.FtpUtil;
import com.example.websocket_service.dao.ComplaintInfoDao;
import com.example.websocket_service.entity.ComplaintInfo;
import com.example.websocket_service.entity.form.ComplaintInfoF;
import com.example.websocket_service.service.ComplaintService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
public class ComplaintImpl implements ComplaintService {

    @Autowired
    private ComplaintInfoDao complaintInfoDao;

    @Override
    public Boolean insert(ComplaintInfoF form) throws Exception {
        String orderNumber = form.getOrderNumber();
        String complainant = form.getComplainant();
        StringBuilder rest = new StringBuilder();
        for (String complaint : form.getComplaints()) {
            rest  = rest.append(complaint).append(",");
        }
        String complaints = rest.deleteCharAt(rest.length()-1).toString();
        if (Objects.nonNull(complaintInfoDao.selectById(orderNumber))){
            throw new Exception("订单号已投诉");
        }
        String isCreated = complaintInfoDao.selectByCreatedTime(complainant,new DateTime());
        if (Objects.nonNull(isCreated)){
            throw new Exception("创建太过频繁，请在5分钟后重试！");
        }
//        List<MultipartFile> pictureList = form.getPictureList();
        String pictureUrl = "";
//        StringBuilder build = new StringBuilder();
        if (Objects.nonNull(form.getPicture())){
            pictureUrl = FtpUtil.uploadFile(form.getPicture1(),true);
        }
        ComplaintInfo info = new ComplaintInfo();
        BeanUtils.copyProperties(form, info);
//        if (CollectionUtils.isEmpty(pictureList)){
//            info.setPicture(null);
//        }else{
//            for (MultipartFile file : pictureList) {
//                String picture = FtpUtil.uploadFile(file);
//                build.append(picture+",");
//            }
//            build.deleteCharAt(build.length()-1);
//            info.setPicture(build.toString());
//        }
        info.setComplaints(complaints);
        info.setPicture(pictureUrl);
        info.setState(0);
        info.setCreator(complainant);
        info.setCreateDate(new DateTime().toString());
        Boolean insert = complaintInfoDao.insert(info);
        return insert;
    }

    @Override
    public ComplaintInfo selectByName(String userName, String orderNumber) {
        ComplaintInfo info = complaintInfoDao.selectByName(userName,orderNumber);
        return info;
    }

    @Override
    public void update(HashMap<String, Object> map) {
        complaintInfoDao.update(map);
    }

}

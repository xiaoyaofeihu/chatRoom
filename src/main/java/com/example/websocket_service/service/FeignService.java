package com.example.websocket_service.service;

import com.example.websocket_service.config.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
//@FeignClient(url = "http://172.16.4.1:18091/admin/Dp-Admin-Server",name = "Dp-Admin-Server")
@FeignClient("DP-ADMIN-SERVER")
public interface FeignService {

    @RequestMapping(value = "sys/user/info",method = RequestMethod.GET)
    public RestResponse user();

}
package com.imokkkk.push.controller;

import com.imokkkk.push.model.Message;
import com.imokkkk.push.service.PushService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuwy
 * @date 2023-05-06 15:19
 * @since 1.0
 */
@RestController
@RequestMapping("/push")
public class PushController {

    @Autowired private PushService pushService;

    @PostMapping("/push")
    public void pushToAll(@RequestBody @Validated Message message) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
        pushService.push(message.complete());
    }
}

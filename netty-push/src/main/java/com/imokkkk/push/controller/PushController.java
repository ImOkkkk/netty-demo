package com.imokkkk.push.controller;

import com.imokkkk.push.model.Message;
import com.imokkkk.push.service.PushService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

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
    //    @Bulkhead(name = "backend-pushToAll")
    @RateLimiter(name = "backend-pushToAll", fallbackMethod = "fallbackPushToAll")
    public String pushToAll(@RequestBody @Validated Message message) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
        pushService.push(message.complete());
        return "SUCCESS!";
    }

    public String fallbackPushToAll(Message message, Throwable t) {
        return "Sorry, the server is busy now.";
    }
}

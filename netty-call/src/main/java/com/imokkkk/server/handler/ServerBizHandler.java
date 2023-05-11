package com.imokkkk.server.handler;

import cn.hutool.core.util.StrUtil;
import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import com.imokkkk.server.processor.AsyncBizProcessor;
import com.imokkkk.server.processor.TaskProcessor;
import com.imokkkk.util.EncryptUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-11 10:40
 * @since 1.0
 */
@Slf4j
public class ServerBizHandler extends SimpleChannelInboundHandler<Message> {

    private TaskProcessor taskProcessor;

    public ServerBizHandler(TaskProcessor taskProcessor) {
        super();
        this.taskProcessor = taskProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        String headMd5 = msg.getHeader().getMd5();
        String calcMd5 = EncryptUtils.encryptObj(msg.getBody());
        Message resp = new Message();
        if (!StrUtil.equals(headMd5, calcMd5)) {
            log.error("报文md5检查不通过：" + headMd5 + " vs " + calcMd5 + "，关闭连接");
            ctx.writeAndFlush(resp.buildBasicMessage("报文md5检查不通过，关闭连接", MessageType.SERVICE_RESP));
            ctx.close();
        }
        log.info(msg.toString());

        if (MessageType.ONE_WAY.same(msg.getHeader())) {
            log.debug("ONE_WAY类型消息，异步处理");
            AsyncBizProcessor.submitTask(taskProcessor.execAsyncTask(msg));
        } else {
            log.debug("TWO_WAY类型消息，应答");
            ctx.writeAndFlush(resp.buildBasicMessage(1, MessageType.SERVICE_RESP));
        }
    }
}

package com.dhls.util.websocket.handler;

import com.dhls.util.websocket.entity.UserChannelEntity;
import com.dhls.util.websocket.link.Global;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 */
public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = Logger.getLogger(WebSocketServerHandshaker.class.getName());
    private WebSocketServerHandshaker handshaker;
    /**
     * channel 通道 action 活跃的 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
//        Global.channel.put("x",ctx.channel());
//        if(!Global.hasChannel(ctx.channel())){
//            // 断开连接
//
//        }
    }
    /**
     * channel 通道 Inactive 不活跃的 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端关闭了通信通道并且不可以传输数据
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        /**
         * 移除
         */
        Global.remove(ctx.channel());
    }
    /**
     * 接收客户端发送的消息 channel 通道 Read 读 简而言之就是从通道中读取数据，也就是服务端接收客户端发来的数据。但是这个数据在不进行解码时它是ByteBuf类型的
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * 传统的HTTP接入
         */
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, ((FullHttpRequest) msg));
            /**
             * WebSocket接入
             */
        } else if (msg instanceof WebSocketFrame) {
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }
    /**
     * channel 通道 Read 读取 Complete 完成 在通道读取完成后会在这个方法里通知，对应可以做刷新操作 ctx.flush()
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        /**
         * 判断是否关闭链路的指令
         */
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        /**
         * 判断是否ping消息
         */
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        /**
         * 本例程仅支持文本消息，不支持二进制消息
         */
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("%s frame types not supported", frame.getClass().getName()));
        }
        /**
         *  返回应答消息
         */
        String request = ((TextWebSocketFrame) frame).text();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("%s received %s", ctx.channel(), request));
        }
        String[] strings = request.split(",");
        TextWebSocketFrame tws = null;
        if(strings[0].equalsIgnoreCase("ping")){
            tws = new TextWebSocketFrame("pong");
        }else{
            tws = new TextWebSocketFrame(strings[0]);
        }

          ctx.channel().writeAndFlush(tws);
    }
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        /**
         * 如果HTTP解码失败，返回HHTP异常
         */
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        /**
         * 获取url后置参数
         */
        HttpMethod method=req.method();
        String uri=req.uri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        String userId = userId = parameters.getOrDefault("userId", Arrays.asList("default")).get(0);
        String topic = parameters.getOrDefault("topic", Arrays.asList("default")).get(0);;
        // 封装
        Global.put(UserChannelEntity.builder().channel(ctx.channel()).topic(topic).userId(userId).build());
        if(method==HttpMethod.GET&&uri.toLowerCase().startsWith("/webssss")){
            ctx.channel().attr(AttributeKey.valueOf("type")).set("anzhuo");
        }else if(method==HttpMethod.GET&&uri.toLowerCase().startsWith("/websocket")){
            ctx.channel().attr(AttributeKey.valueOf("type")).set("live");
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(ctx.pipeline(),req,uri), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }
    private static void sendHttpResponse(ChannelHandlerContext ctx, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!ctx.channel().isActive() || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
    /**
     * exception 异常 Caught 抓住 抓住异常，当发生异常的时候，可以做一些相应的处理，比如打印日志、关闭链接
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.ALL,cause.getMessage());
        ctx.close();
        // 关闭
        Global.remove(ctx.channel());
    }
    private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
        String protocol = "ws";
        if (cp.get(SslHandler.class) != null) {
            // SSL in use so use Secure WebSockets
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + path;
    }

}

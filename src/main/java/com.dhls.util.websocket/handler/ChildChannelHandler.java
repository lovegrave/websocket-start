package com.dhls.util.websocket.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * @author
 */
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    public ChildChannelHandler(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast("http-codec",
                new HttpServerCodec());
        pipeline.addLast("aggregator",
                new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-chunked",
                new ChunkedWriteHandler());
        pipeline.addLast("handler",
                new MyWebSocketServerHandler());
    }
}

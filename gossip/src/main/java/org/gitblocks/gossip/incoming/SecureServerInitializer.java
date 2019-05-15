package org.gitblocks.gossip.incoming;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class SecureServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private IncomingConnectionManager incomingConnectionManager;

    public SecureServerInitializer(SslContext ctx, IncomingConnectionManager connectionManager) {
        this.sslCtx = ctx;
        this.incomingConnectionManager = connectionManager;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.err.println("Channel inactive "+ ctx.channel().remoteAddress() );
    }

    protected void initChannel(final SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();

        // SSL handler decrypts and encrypts data
        pipeline.addFirst( sslCtx.newHandler(socketChannel.alloc()) );

        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // finally the stream handler
        pipeline.addLast(new SecureServerHandler(incomingConnectionManager)); // TODO: avoid circular dependencies

        ChannelFuture closeFuture = socketChannel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                System.err.println("Channel closed "+ socketChannel.remoteAddress() );
            }
        });
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.err.println("Channel unregistered: "+ ctx.channel().remoteAddress());
    }

    public String getNodeName() {
        return incomingConnectionManager.getNodeName();
    }
}

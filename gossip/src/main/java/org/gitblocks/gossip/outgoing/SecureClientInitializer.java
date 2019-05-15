package org.gitblocks.gossip.outgoing;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import org.gitblocks.gossip.incoming.IncomingConnectionManager;

public class SecureClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;
    private OutgoingConnectionManager outgoingConnectionManager;

    public SecureClientInitializer(SslContext ctx, OutgoingConnectionManager connectionManager ){
        this.sslContext = ctx;
        this.outgoingConnectionManager = connectionManager;
    }

    protected void initChannel(SocketChannel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(sslContext.newHandler(channel.alloc(), OutgoingConnectionManager.LOCAL_HOST, IncomingConnectionManager.PORT ));

        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // Handler that processes that outgoing events
        pipeline.addLast(new SecureClientHandler( outgoingConnectionManager)); // TODO: avoid circular dependencies
    }

}

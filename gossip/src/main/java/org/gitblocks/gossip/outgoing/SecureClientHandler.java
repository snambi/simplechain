package org.gitblocks.gossip.outgoing;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.gitblocks.gossip.MessageRouter;
import org.gitblocks.gossip.MessageWrapper;
import org.gitblocks.gossip.Starter;

public class SecureClientHandler extends SimpleChannelInboundHandler<String> {

    private OutgoingConnectionManager outgoingConnectionManager;

    public SecureClientHandler(OutgoingConnectionManager outgoingConnectionManager) {
        this.outgoingConnectionManager = outgoingConnectionManager;
    }

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        MessageWrapper wrapper = MessageWrapper.fromJson(msg);

        // TODO: properly pass this object via constructor.
        MessageRouter messageRouter = Starter.getMessageRouter();
        messageRouter.process(ctx, wrapper);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

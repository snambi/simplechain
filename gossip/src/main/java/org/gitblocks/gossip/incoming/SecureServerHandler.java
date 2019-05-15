package org.gitblocks.gossip.incoming;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.gitblocks.gossip.MessageRouter;
import org.gitblocks.gossip.MessageWrapper;
import org.gitblocks.gossip.Starter;

public class SecureServerHandler extends SimpleChannelInboundHandler<String> {

    //static final ChannelGroup incomingChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private IncomingConnectionManager incomingConnectionManager;


    public SecureServerHandler(IncomingConnectionManager connectionManager) {
        this.incomingConnectionManager = connectionManager;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {

        // when the connection happens,
        // right after the execution of SSL handler,
        // a welcome message is sent to the client

        ctx.pipeline()
                .get(SslHandler.class)
                .handshakeFuture()
                .addListener(new GenericFutureListener<Future<? super Channel>>() {

                    public void operationComplete(Future<? super Channel> future) throws Exception {

                        System.out.println("Received connection from : "+ ctx.channel().remoteAddress());

                        if( Starter.isDebug() ) {
                            System.out.println("Sending to : " + ctx.channel().remoteAddress());
                        }

                        // Send a welcome message back to the client
//                        String msg = String.format("Welcome to %s secure stream. The connection is protected by %s.",
//                                incomingConnectionManager.getNodeName(),
//                                ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite());

//                        org.gitblocks.gossip.Message m = org.gitblocks.gossip.Message.create(incomingConnectionManager.getNodeName(), msg );
//                        org.gitblocks.gossip.MessageWrapper wrapper = new org.gitblocks.gossip.MessageWrapper(m.toJson());
//
//                        ctx.writeAndFlush( wrapper.toJson() + "\n");

                        incomingConnectionManager.getIncomingChannels().add(ctx.channel());
                    }
                });
    }

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
            ctx.channel().writeAndFlush("[you] "+ msg + '\n');
            ctx.close();
            return;
        }

        MessageWrapper wrapper = MessageWrapper.fromJson(msg);

        // TODO: properly pass this object via constructor.
        MessageRouter messageRouter = Starter.getMessageRouter();
        messageRouter.process(ctx, wrapper);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

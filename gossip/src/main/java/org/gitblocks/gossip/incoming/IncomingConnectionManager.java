package org.gitblocks.gossip.incoming;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.gitblocks.gossip.Message;
import org.gitblocks.gossip.MessageWrapper;
import org.gitblocks.gossip.RandomString;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * Manages all outgoing connections.
 *
 */
public class IncomingConnectionManager {

    //private static final Logger logger = LoggerFactory.getLogger(IncomingConnectionManager.class);

    public static final int PORT = Integer.parseInt(System.getProperty("port", "9002"));
    ChannelGroup incomingChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Thread runner;
    private String nodeName;

    public static void main( String[] args ){
        IncomingConnectionManager server = new IncomingConnectionManager();
        server.startListening(PORT);
    }

    public IncomingConnectionManager(){
        this(RandomString.generateString(5));
    }

    public IncomingConnectionManager(String nodeName ){
        this.nodeName = nodeName;
    }

    public void startListening(int port ){

        try {

            SelfSignedCertificate cert = new SelfSignedCertificate();
            SslContext sslContext = SslContextBuilder.forServer( cert.certificate(), cert.privateKey()).build();

            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler( new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new SecureServerInitializer(sslContext, this)); // TODO: avoid circular dependencies

            System.out.println("Listening for connections on "+ port + " server_name: "+ getNodeName());

            bootstrap.bind( port )
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();

        } catch (CertificateException | SSLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start(final int port){

        Runnable r = new Runnable() {
            public void run() {
                startListening( port);
            }
        };

        runner = new Thread(r);
        runner.start();
    }

    public void broadcast( ChannelHandlerContext handlerContext, MessageWrapper messageWrapper ){

        for( Channel channel : getIncomingChannels()){

            if( handlerContext.channel() != channel ){

                Message message = messageWrapper.getMessage();
                //System.out.println("RECD >> "+ message.getContent());
                channel.writeAndFlush( messageWrapper.toJson() + '\n' );


            }else{
                // This is the channel, on which the message came.
                // So, don't write anything back
                // TODO: we may need a way to acknowledge the receipt of the message
                //channel.writeAndFlush("[you] "+ msg + '\n');
            }
        }
    }

    String getNodeName() {
        return nodeName;
    }

    ChannelGroup getIncomingChannels() {
        return incomingChannels;
    }

    public void waitForCompletion(){
        try {
            runner.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

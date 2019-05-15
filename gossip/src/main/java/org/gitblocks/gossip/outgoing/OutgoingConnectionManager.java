package org.gitblocks.gossip.outgoing;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.gitblocks.gossip.*;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.gitblocks.gossip.Starter.PORT;


/**
 *
 * Manages all incoming connections.
 *
 * Gossip Client is responsible for
 *
 * <ul>
 *     <li>Connecting to other nodes that are passed via CLI parameters. These are incoming connections</li>
 *     <li>Also Connects to the default listener running on this process</li>
 *     <li>Constantly reads the STDIN and sends the message to the local listener</li>
 * </ul>
 */
public class OutgoingConnectionManager {

    //private static final Logger logger = LoggerFactory.getLogger(OutgoingConnectionManager.class);

    public static final String LOCAL_HOST = System.getProperty("host", "127.0.0.1");

    private int localPort;
    List<RemoteNode> nodes = new ArrayList<>();

    private Channel localChannel = null;
    private NioEventLoopGroup localLoopGroup = null;
    private NioEventLoopGroup remoteLoopGroup = null;

    private List<Channel> outgoingChannels = new ArrayList<Channel>();
    //private ChannelGroup outgoing = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private String nodeName;

    /**
     * Connects only to local host, on a given port.
     * If the port is passed as 0, it connects on the default port.
     * @param localPort
     */
    public OutgoingConnectionManager(int localPort){
        this( RandomString.generateString(5), localPort, null);
    }

    public OutgoingConnectionManager(String nodeName, int localPort, String remoteHosts ){

        if( localPort == 0 ){
            localPort = PORT;
        }

        this.localPort = localPort;
        this.nodeName = nodeName;

        // add the local connection
        RemoteNode localNode = new RemoteNode("127.0.0.1", localPort);

        nodes.add(localNode);

        if( remoteHosts != null ) {
            nodes.addAll(RemoteNode.parseMultiHosts(remoteHosts));
        }
    }

    /**
     * Connects to localhost and remote hosts
     * @param remoteHosts
     * @param localPort
     */
    public OutgoingConnectionManager(String remoteHosts, int localPort ) {
        nodes = RemoteNode.parseMultiHosts(remoteHosts);
        this.localPort = localPort;
    }

    public static void main(String[] args ){
        OutgoingConnectionManager client = new OutgoingConnectionManager(LOCAL_HOST, PORT );
        client.start();
    }


    public void start(){

        startRemoteChannels();
        startCli();
    }


    public void startCli(){

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        for (;;) {

            String line = null;

            try {
                line = in.readLine();

                Message message = Message.create(nodeName, line);
                MessageWrapper wrapper = new MessageWrapper(message.toJson());

                //sendLocal( wrapper.toJson() );
                broadcast( wrapper.toJson() );

                if (line == null) {
                    break;
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void startRemoteChannels(){

        if( nodes == null || nodes.isEmpty() ){
            return;
        }

        try{

            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            remoteLoopGroup = new NioEventLoopGroup(10);

            for( RemoteNode node : nodes ) {

                Bootstrap bootstrap = new Bootstrap();

                bootstrap.group(remoteLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new SecureClientInitializer(sslContext, this)); // TODO: avoid circular dependencies

                System.out.println("Remote connection to " + node.getHost() + ":" + node.getPort());

                Channel remoteChannel = bootstrap.connect( node.getHost(), node.getPort()).sync().channel();

                // add the remote channels to the list for future reference
                outgoingChannels.add(remoteChannel);

                //outgoing.add(remoteChannel);
            }

        }catch (InterruptedException | SSLException e) {
            e.printStackTrace();
        } finally{
        }
    }

    /**
     * Connects the local listening socket to send messages.
     */
    @Deprecated
    public void startLocalChannel(){

        final SslContext sslContext;

        localLoopGroup = new NioEventLoopGroup();

        try {

            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(localLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler( new SecureClientInitializer( sslContext, null ));

            System.out.println("Local connection to "+ LOCAL_HOST + ":" + localPort );

            localChannel = bootstrap.connect(LOCAL_HOST, localPort ).sync().channel();

        } catch (SSLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void sendLocal( String msg ) throws InterruptedException {

        ChannelFuture lastWriteFuture = localChannel.writeAndFlush(msg + "\r\n");

        if( lastWriteFuture != null ){
            lastWriteFuture.sync();
        }

        if( msg.toLowerCase().equals("bye")){
            localChannel.closeFuture().sync();
        }
    }

    public void broadcast(String msg ) throws InterruptedException {

        for( Channel channel : outgoingChannels){
        //for(Channel channel: outgoing){

            if( channel == localChannel){
                continue;
            }

            if( Starter.isDebug() ){
                System.out.println("Sending remote message to [ "+ channel.remoteAddress() + "]"+ msg);
            }

            ChannelFuture channelFuture = channel.writeAndFlush(msg + '\n');

            if( channelFuture != null ){
                channelFuture.sync();
            }

            if( msg.toLowerCase().equals("bye")){
                channel.closeFuture().sync();
            }
        }
    }

    public void broadcast(MessageWrapper wrapper){
        if( wrapper != null ){
            try {
                broadcast(wrapper.toJson());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdownLocal(){
        localLoopGroup.shutdownGracefully();
    }
}
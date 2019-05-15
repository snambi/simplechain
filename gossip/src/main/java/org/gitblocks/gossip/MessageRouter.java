package org.gitblocks.gossip;

import io.netty.channel.ChannelHandlerContext;
import org.gitblocks.gossip.incoming.IncomingConnectionManager;
import org.gitblocks.gossip.outgoing.OutgoingConnectionManager;

public class MessageRouter {

    private IncomingConnectionManager incomingConnectionManager;
    private OutgoingConnectionManager outgoingConnectionManager;

    private MessageStorage storage;


    public MessageRouter(IncomingConnectionManager incomingConnectionManager,
                         OutgoingConnectionManager outgoingConnectionManager) {

        this.incomingConnectionManager = incomingConnectionManager;
        this.outgoingConnectionManager = outgoingConnectionManager;

        this.storage = new MessageStorage();
    }

    /**
     * Handles all incoming messages, from both incoming and outgoing channels.
     *
     * <ol>
     *     <li>check whether the message is already received</li>
     *     <li>broadcast on incoming and outgoing channels</li>
     *     <li>add the message to received messages list</li>
     * </ol>
    */
    public synchronized void process(ChannelHandlerContext handlerContext, MessageWrapper messageWrapper){

        if( messageWrapper == null ){
            return;
        }

        if( storage.containsMessage(messageWrapper.getMessage())){
            if( Starter.isDebug())
                System.out.println("message found: " + messageWrapper.getMessage().getContent());

            return;
        }
        storage.add(messageWrapper.getMessage());

        System.out.println(">>> "+ messageWrapper.getMessage().getContent() );

        broadcast(handlerContext, messageWrapper);
    }


    /**
     * Sends the message to all incoming and outgoing channels
     * @param messageWrapper
     */
    public void broadcast(ChannelHandlerContext handlerContext, MessageWrapper messageWrapper ){


        // TODO: use a threadpool
        Runnable r = new Runnable() {
            @Override
            public void run() {
                incomingConnectionManager.broadcast(handlerContext, messageWrapper);
                if( Starter.isDebug()){
                    System.out.println("Broadcast on incoming");
                }
            }
        };

        Thread t1 = new Thread(r);
        t1.start();

        Runnable s = new Runnable() {
            @Override
            public void run() {
                outgoingConnectionManager.broadcast(messageWrapper);
                if( Starter.isDebug()) {
                    System.out.println("Broadcast on outgoing");
                }
            }
        };

        Thread t2 = new Thread(s);
        t2.start();
    }
}

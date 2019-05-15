package org.gitblocks.gossip;

import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Any information that is sent between nodes will be encapsulated by a org.gitblocks.gossip.Message wrapper.
 */
public class Message {

    private String sender;
    private InetAddress inetAddress;
    private List<String> receivers = new ArrayList<>();
    private String id;
    private MessageType messageType = MessageType.NORMAL;
    private Date timestamp;
    private String content;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this, Message.class);
    }

    public static Message fromJson( String json ){
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }

    public static Message create(String sender, String msg){

        Message message = new Message();

        try {

            message.setTimestamp(Calendar.getInstance().getTime());
            message.setId(UUID.randomUUID().toString());
            message.setInetAddress(InetAddress.getLocalHost());

            message.setSender(sender);
            message.setContent(msg);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return message;
    }
}

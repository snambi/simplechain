package org.gitblocks.gossip;

import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

public class MessageWrapper {

    private String json;
    private String hash;

    public MessageWrapper(String json) {
        this.json = json;

        // generate hash based on the JSON
        this.hash = DigestUtils.sha256Hex(json);
    }

    public String getJson() {
        return json;
    }

    public String getHash() {
        return hash;
    }

    public Message getMessage(){
        return Message.fromJson(getJson());
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this, MessageWrapper.class);
    }

    public static MessageWrapper fromJson(String json ){
        Gson gson = new Gson();
        return gson.fromJson(json, MessageWrapper.class);
    }

    public static Message extractMessageFromJson( String json ){
        Gson gson = new Gson();
        MessageWrapper wrapper =  gson.fromJson(json, MessageWrapper.class);
        return wrapper.getMessage();
    }
}

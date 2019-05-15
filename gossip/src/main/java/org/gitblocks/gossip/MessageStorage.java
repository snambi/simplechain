package org.gitblocks.gossip;

import org.gitblocks.gossip.Message;

import java.util.Map;
import java.util.TreeMap;

public class MessageStorage {

    // Useful for searching the a message faster
    private Map<String, Message> messages = new TreeMap<>();

    // TODO: need to store the messages in the chronological order

    public boolean containsMessage( Message message ){
        return messages.containsKey(message.getId());
    }

    public void add( Message message){
        if( !containsMessage(message)){
            messages.put(message.getId(), message);
        }
    }
}

package org.gitblocks.gossip;

import java.util.ArrayList;
import java.util.List;

import static org.gitblocks.gossip.Starter.PORT;


public class RemoteNode {

    private String name;
    private String host;
    private int port;

    public RemoteNode(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public RemoteNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static RemoteNode parseStr(String remoteNodeStr ){

        RemoteNode node = null;

        if( remoteNodeStr != null && !remoteNodeStr.isEmpty()){

            if( remoteNodeStr.contains(":")){

                String[] arr = remoteNodeStr.split(":");
                if( arr.length == 2 ){
                    int port = Integer.parseInt( arr[1] );
                    node = new RemoteNode(arr[0], port);
                }
            }else{
                node = new RemoteNode(remoteNodeStr, PORT );
            }
        }

        return node;
    }

    public static List<RemoteNode> parseMultiHosts( String data ){

        List<RemoteNode> nodes = new ArrayList<RemoteNode>();

        if( data != null && !data.isEmpty()){
            if( data.contains(",")){
                String[] arr = data.split(",");
                if( arr.length > 1){
                    for( String a : arr ){
                        RemoteNode n = parseStr(a);
                        nodes.add(n);
                    }
                }
            }else{
                RemoteNode n = parseStr(data);
                nodes.add(n);
            }
        }

        return nodes;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

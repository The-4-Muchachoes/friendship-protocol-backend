package com.muchachos.friendshipprotocol.Friendship.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDTO {

    public static final List<String> VALID_METHODS = List.of("ADD", "ACCEPT", "DENY", "REMOVE", "BLOCK");
    public static final String HOST = "192.168.x.xxx:8080";
    public static final String VERSION = "1";

    private String method;
    private String src;
    private String srcHost;
    private String dest;
    private String destHost;
    private String version;

    public boolean isValid() {
        if (!VALID_METHODS.contains(method.toUpperCase()))
            return false;

        // TODO: Add more checks

        return true;
    }

    public void swapSrcAndDest() {
        String src = this.src;
        String srcHost = this.srcHost;
        String dest = this.dest;
        String destHost = this.destHost;

        this.src = dest;
        this.srcHost = destHost;
        this.dest = src;
        this.destHost = srcHost;
    }
}

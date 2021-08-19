package edu.sdsu.its.video_inv.API;

import com.google.gson.annotations.Expose;

/**
 * Simple JSON Object that can be used to send a message when a JSON object is expected by the Client.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@SuppressWarnings("WeakerAccess")
public class SimpleMessage {
    @Expose
    private String status = null;
    @Expose
    private String message;

    public SimpleMessage(String message) {
        this.message = message;
    }

    public SimpleMessage(String status, String message) {
        this.status = status;
        this.message = message;
    }
}

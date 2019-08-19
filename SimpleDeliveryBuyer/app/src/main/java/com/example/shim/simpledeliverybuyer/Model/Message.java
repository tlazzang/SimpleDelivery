package com.example.shim.simpledeliverybuyer.Model;

public class Message {
    int id;
    int sender_id;
    int receiver_id;
    String contents;
    long timestamp;
    int errand_id;

    public Message() {
    }

    public Message(int id, int sender_id, int receiver_id, String contents, long timestamp) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.contents = contents;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getErrand_id() {
        return errand_id;
    }

    public void setErrand_id(int errand_id) {
        this.errand_id = errand_id;
    }
}

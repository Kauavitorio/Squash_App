package dev.kaua.squash.Data.Message;

public class Chatslist {
    private String id;
    private String chat_id;

    public Chatslist(String id, String chat_id) {
        this.id = id;
        this.chat_id = chat_id;
    }
    public Chatslist() {}

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

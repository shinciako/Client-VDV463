package com.vdv463.client.communication;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class MessageHandlerImpl implements MessageHandler {

    private List<Message> messages = new ArrayList<>();

    public void swap(){
        messages = new ArrayList<>();
    }


    @Override
    public void onMessage(Message message) {
        messages.add(message);
    }
}

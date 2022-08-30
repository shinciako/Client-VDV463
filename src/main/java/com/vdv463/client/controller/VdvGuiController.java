package com.vdv463.client.controller;

import com.vdv463.client.VdvService;
import com.vdv463.client.communication.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.com.ekoenergetyka.vdv463.messages.ChargingRequest;

@Slf4j
@RequestMapping("/vdv463-simG")
@Controller
public class VdvGuiController {
    @Autowired
    private VdvService vdvService;


    @GetMapping("/messages")
    public String getMessages(Model model) {
        model.addAttribute("newMessage", new Message());
        model.addAttribute("newChargingRequestList",new ChargingRequest());
        model.addAttribute("messageList", vdvService.getMessages());
        model.addAttribute("isOpen", vdvService.isOpen());
        model.addAttribute("newDates", vdvService.createDateDto());
        return "messages";
    }


}

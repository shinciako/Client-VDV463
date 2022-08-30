package com.vdv463.client.controller;

import com.vdv463.client.DateDto;
import com.vdv463.client.VdvService;
import com.vdv463.client.communication.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import pl.com.ekoenergetyka.vdv463.messages.ChargingRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/vdv463-sim")
public class VdvRestController {

    @Autowired
    private VdvService vdvService;

    @RequestMapping("/connection")
    public ResponseEntity<?> wsConnect(WebRequest request) throws Exception {
        return vdvService.connectToServer(request);
    }

    @RequestMapping("/exit")
    public void closeConnection() throws IOException {
        vdvService.close();
    }

    @RequestMapping("/messages/new/boot")
    public ResponseEntity<?> sendBootRequest() {
        return vdvService.sendBootRequest(Message.builder()
                .type(Message.Type.REQUEST)
                .source("BMS")
                .presystemId("jeden")
                .action("BootNotification")
                .build());
    }

    @RequestMapping("/messages/new/charge")
    public ResponseEntity<?> sendChargeRequest() throws URISyntaxException {
        return vdvService.sendChargingRequest();
    }

    @PostMapping("/messages/new")
    public ResponseEntity<?> createCustomMessage(@ModelAttribute("newMessage") Message message,
                                                       @ModelAttribute("newChargingRequestList") ChargingRequest chargingRequest,
                                                       @ModelAttribute("newDates") DateDto dateDto) {
        log.info("preconditioning - {}", chargingRequest.getAutomaticPreconditioning().getPreconditioningRequest());
        return vdvService.sendCustomMessage(message, chargingRequest, dateDto);
    }

    @GetMapping("/messages")
    public List<Message> getMessages() {
        return vdvService.getMessages();
    }


    @GetMapping("/liquidation")
    public void liquidateMessages(HttpServletResponse response) throws IOException {
        vdvService.liquidate();
        response.sendRedirect("/vdv463-simG/messages");
    }



//    @GetMapping("/liquidation")
//    public ResponseEntity<?> liquidateMessages() {
//        vdvService.liquidate();
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}

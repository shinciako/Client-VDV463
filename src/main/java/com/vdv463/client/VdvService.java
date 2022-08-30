package com.vdv463.client;

import com.vdv463.client.communication.Message;
import com.vdv463.client.communication.MessageHandlerImpl;
import com.vdv463.client.exception.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import pl.com.ekoenergetyka.vdv463.messages.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class VdvService {
    private final MessageHandlerImpl handler = new MessageHandlerImpl();

    private WebSocketClient webSocketClient;

    public ResponseEntity<?> connectToServer(WebRequest request) throws Exception {
        if (!isOpen()) {
            return new ResponseEntity<>(new ErrorDetails(new Date(), "Websocket already connected", request.getDescription(false)), HttpStatus.BAD_REQUEST);
        }
        webSocketClient = new WebSocketClient();
        webSocketClient.connect(webSocketClient.wsConfig);
        webSocketClient.attach(handler);
        return new ResponseEntity<>(HttpStatus.SWITCHING_PROTOCOLS);
    }

    public ResponseEntity<?> sendChargingRequest() throws URISyntaxException {
        ProvideChargingRequestsRequest pcrReq = new ProvideChargingRequestsRequest();
        List<ChargingRequest> ChargingRequestList = new ArrayList<>();

        ChargingRequestData chargingRequestData = new ChargingRequestData();
        chargingRequestData.setExpectedArrivalTimeAtChargingPoint(ZonedDateTime.of(LocalDateTime.now().plusMinutes(5), ZoneId.of("Z")));
        chargingRequestData.setRequestedTimeForDeparture(ZonedDateTime.of(LocalDateTime.now().plusMinutes(50), ZoneId.of("Z")));
        chargingRequestData.setExpectedSocAtArrival(22.0);
        chargingRequestData.setMinTargetSoc(85.0);
        chargingRequestData.setMaxTargetSoc(90.0);
        chargingRequestData.setAdHocCharging(false);


        AutomaticPreconditioning automaticPreconditioning = new AutomaticPreconditioning();
        automaticPreconditioning.setPreconditioningRequest(AutomaticPreconditioning.PreconditioningRequest.HOT_WATER_AND_HEATING);
        automaticPreconditioning.setAmbientTemperature(20.0);
        automaticPreconditioning.setRequestedFinishTime(ZonedDateTime.of(LocalDateTime.now().plusMinutes(50), ZoneId.of("Z")));

        ChargingRequest chargingRequest = new ChargingRequest();
        chargingRequest.setChargingPointId(new URI("Customer1/Depot1/CS1/CP1"));
        chargingRequest.setVehicleId("VIN12345678901234");
        chargingRequest.setChargingRequestId(new URI("Customer1/Presystem1/Depot1/CR1"));
        chargingRequest.setChargingProcessId(new URI("Customer1/CPR1"));
        chargingRequest.setPriority(1);
        chargingRequest.setChargingInstruction(ChargingRequest.ChargingInstruction.CHANGED);
        chargingRequest.setChargingRequestData(chargingRequestData);
        chargingRequest.setAutomaticPreconditioning(automaticPreconditioning);

        ChargingRequestList.add(chargingRequest);
        pcrReq.setChargingRequestList(ChargingRequestList);
        Message message = Message.builder()
                .type(Message.Type.REQUEST)
                .source("BMS")
                .presystemId("jeden")
                .action("ProvideChargingRequests")
                .objectPayload(pcrReq)
                .build();
        setGeneratedAttributes(message);
        sendMessage(message);
        log.info(String.valueOf(message));
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    private ResponseEntity<?> sendChargingRequest(Message message, ChargingRequest chargingRequest, DateDto dateDto) {
        ProvideChargingRequestsRequest pcrReq = new ProvideChargingRequestsRequest();
        List<ChargingRequest> chargingRequestList = new ArrayList<>();
        if (dateDto!=null){
            ChargingRequestData chargingRequestData = chargingRequest.getChargingRequestData();
            if(dateDto.dateConverters.get(0).date != null){
                chargingRequestData.setExpectedArrivalTimeAtChargingPoint(dateDto.dateConverters.get(0).convertToZonedDateTime());
            }
            if(dateDto.dateConverters.get(1).date != null){
                chargingRequestData.setRequestedTimeForDeparture(dateDto.dateConverters.get(1).convertToZonedDateTime());
            }
            if(dateDto.dateConverters.get(2).date !=null){
                chargingRequest.getAutomaticPreconditioning().setRequestedFinishTime(dateDto.dateConverters.get(2).convertToZonedDateTime());
            }
            chargingRequestList.add(chargingRequest);
        }



        pcrReq.setChargingRequestList(chargingRequestList);
        message.setObjectPayload(pcrReq);
        setGeneratedAttributes(message);
        sendMessage(message);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    public ResponseEntity<?> sendBootRequest(Message message) {
        setGeneratedAttributes(message);
        BootNotificationRequest objectPayload = new BootNotificationRequest();
        objectPayload.setPresystem(BootNotificationRequest.PresystemEnumType.BMS);
        message.setObjectPayload(objectPayload);
        sendMessage(message);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    public ResponseEntity<?> sendCustomMessage(Message message, ChargingRequest chargingRequest, DateDto dateDto) {
        if (message.getAction().equals("BootNotification")) {
            return sendBootRequest(message);
        }
        return sendChargingRequest(message, chargingRequest, dateDto);
    }

    synchronized private void sendMessage(Message mes) {
        webSocketClient.sendCall(mes);
        webSocketClient.notifyEveryone(mes);
    }

    private void setGeneratedAttributes(Message message) {
        message.setTimeStamp(ZonedDateTime.of(LocalDateTime.now().minusHours(2), ZoneId.of("Z")));
        message.setId(UUID.randomUUID().toString());
    }

    public List<Message> getMessages() {
        return handler.getMessages();
    }

    public void liquidate() {
        handler.swap();
    }

    public void close() throws IOException {
        webSocketClient.close();
        webSocketClient = null;
    }

    public boolean isOpen() {
        return webSocketClient == null;
    }

    public DateDto createDateDto() {
        DateDto datesForm = new DateDto();
        for (int i = 0; i < 3; i++) {
            datesForm.addDateConverter(new DateConverter());
        }
        return datesForm;
    }
}

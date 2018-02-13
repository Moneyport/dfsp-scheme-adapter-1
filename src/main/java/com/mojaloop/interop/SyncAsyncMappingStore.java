package com.mojaloop.interop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SyncAsyncMappingStore {

    private ConcurrentHashMap<String,String> map;

    private static final Logger log = LoggerFactory.getLogger(SyncAsyncMappingStore.class);

    public SyncAsyncMappingStore(){
        map = new ConcurrentHashMap<String,String>();
    }

    public String getParticipant(String correlationId) {
        log.info("In getParticipant: "+correlationId+";"+ map.get(correlationId));
        return map.get(correlationId);
    }

    public void setParticipantsMap(ConcurrentHashMap<String, String> participantsMap) {
        this.map = participantsMap;
    }

    public void addParticipant(String correlationId,String participantJson){
        log.info("In add participant: "+correlationId+";"+participantJson);
        map.put(correlationId,participantJson);

    }

    public void addQuote(String correlationId,String quoteJson){
        log.info("In add quote: "+correlationId+";"+quoteJson);
        map.put(correlationId,quoteJson);
    }

    public String getQuote(String correlationId) {
        log.info("In getQuote: "+correlationId+";"+ map.get(correlationId));
        return map.get(correlationId);
    }

    public void addPaymentFulfillResponse(String correlationId,String fulfillResponse){
        log.info("In addPaymentFulfillResponse: "+correlationId+";"+fulfillResponse);
        map.put(correlationId,fulfillResponse);
    }

    public String getPaymentFulfillResponse(String correlationId) {
        log.info("In getPaymentFulfillResponse: "+correlationId+";"+ map.get(correlationId));
        return map.get(correlationId);
    }

}

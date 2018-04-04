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
        return map.get(correlationId);
    }

    public void setParticipantsMap(ConcurrentHashMap<String, String> participantsMap) {
        this.map = participantsMap;
    }

    public void addParticipant(String correlationId,String participantJson){
        map.put(correlationId,participantJson);

    }

    public void addQuote(String correlationId,String quoteJson){
        map.put(correlationId,quoteJson);
    }

    public String getQuote(String correlationId) {
        return map.get(correlationId);
    }

    public void addPaymentFulfillResponse(String correlationId,String fulfillResponse){
        map.put(correlationId,fulfillResponse);
    }

    public String getPaymentFulfillResponse(String correlationId) {
        return map.get(correlationId);
    }

}

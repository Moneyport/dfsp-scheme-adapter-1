package com.mojaloop.interop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SyncAsyncMappingStore {

    private ConcurrentHashMap<String,String> participantsMap;

    private static final Logger log = LoggerFactory.getLogger(SyncAsyncMappingStore.class);

    public SyncAsyncMappingStore(){
        participantsMap = new ConcurrentHashMap<String,String>();
    }

    public String getParticipant(String correlationId) {
        log.info("In getParticipant: "+correlationId+";"+participantsMap.get(correlationId));
        return participantsMap.get(correlationId);
    }

    public void setParticipantsMap(ConcurrentHashMap<String, String> participantsMap) {
        this.participantsMap = participantsMap;
    }

    public void addParticipant(String correlationId,String participantJson){
        log.info("In add participant: "+correlationId+";"+participantJson);
        participantsMap.put(correlationId,participantJson);

    }

}

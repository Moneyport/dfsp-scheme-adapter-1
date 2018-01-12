package com.mojaloop.interop;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OverideInboundProperties implements Callable {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        message.setProperty("Content-Type","application/json", PropertyScope.INBOUND);

        return message;
    }

}
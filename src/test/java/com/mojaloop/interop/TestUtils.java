package com.mojaloop.interop;

import com.ilp.conditions.impl.IlpConditionHandlerImpl;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.logging.Logger;


public class TestUtils extends TestCase {

    Logger log = Logger.getLogger("TestUtils.class");

    public void testGetMojaloopPartiesResponse(){
        String testJson = "{\"type\":\"payee\",\"name\":\"bob dylan\",\"firstName\":\"bob\",\"lastName\":\"dylan\",\"nationalId\":\"123456789\",\"dob\":\"1999-12-10\",\"account\":\"http://ec2-52-32-130-4.us-west-2.compute.amazonaws.com:8014/ledger/accounts/bob\",\"currencyCode\":\"TZS\",\"currencySymbol\":\"TSh\",\"imageUrl\":\"https://red.ilpdemo.org/api/receivers/bob_dylan/profile_pic.jpg\"}";
        log.info("Response: "+Utils.createMojaloopPartiesResponse(testJson,"123456"));
    }

    public void testCreateDFSPQuotesRequest() {
        String testJson = "{\"quoteId\":\"1df3c7df-8b3e-4d38-9579-ecc8be12b1fb\",\"transactionId\":\"16687cc9-3439-41e8-a270-ed062daa6328\",\"payee\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"256123456\",\"fspId\":\"21\"}},\"payer\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"25620100001\",\"fspId\":\"20\"},\"personalInfo\":{\"complexName\":{\"firstName\":\"Mats\",\"lastName\":\"Hagman\"},\"dateOfBirth\":\"1983-10-25\"}},\"amountType\":\"RECEIVE\",\"amount\":{\"amount\":\"100\",\"currency\":\"USD\"},\"transactionType\":{\"scenario\":\"TRANSFER\",\"initiator\":\"PAYER\",\"initiatorType\":\"CONSUMER\"},\"note\":\"hej\"}";
        log.info("DFSPQuotesRequest: "+Utils.createDFSPQuotesRequest(testJson));
    }

    public void testCreateMojaloopQuotesResponse() throws Exception{
        String dfspQuotesResponse = "{\"paymentId\":\"1df3c7df-8b3e-4d38-9579-ecc8be12b1fb\",\"expiresAt\":\"2018-01-11T01:55:04.879Z\",\"payeeFee\":{\"amount\":0,\"currency\":\"USD\"},\"payeeCommission\":{\"amount\":0,\"currency\":\"USD\"},\"data\":{\"paymentId\":\"1df3c7df-8b3e-4d38-9579-ecc8be12b1fb\",\"identifier\":\"256123456\",\"identifierType\":\"eur\",\"destinationAccount\":\"http://host/ledger/accounts/alice\",\"currency\":\"USD\",\"fee\":0,\"commission\":0,\"transferType\":\"p2p\",\"amount\":100,\"params\":{\"peer\":{\"identifier\":\"25620100001\",\"identifierType\":\"eur\"}},\"isDebit\":false,\"expiresAt\":\"2018-01-11T01:55:04.879Z\"}}";
        String originalMojaloopQuotesRequest = "{\"quoteId\":\"1df3c7df-8b3e-4d38-9579-ecc8be12b1fb\",\"transactionId\":\"16687cc9-3439-41e8-a270-ed062daa6328\",\"payee\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"27713803905\",\"fspId\":\"21\"}},\"payer\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"25620100001\",\"fspId\":\"20\"},\"personalInfo\":{\"complexName\":{\"firstName\":\"Mats\",\"lastName\":\"Hagman\"},\"dateOfBirth\":\"1983-10-25\"}},\"amountType\":\"RECEIVE\",\"amount\":{\"amount\":\"100\",\"currency\":\"USD\"},\"transactionType\":{\"scenario\":\"TRANSFER\",\"initiator\":\"PAYER\",\"initiatorType\":\"CONSUMER\"},\"note\":\"hej\"}";
        log.info("MojaloopQuotesResponse: "+Utils.createMojaloopQuotesResponse(dfspQuotesResponse,originalMojaloopQuotesRequest));
    }

    public void testFulfillment(){
        IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
        String rawFulfillment = ilpConditionHandlerImpl.generateFulfillment("AQAAAAAAAABkCnByaXZhdGUuMjGCAkl7InRyYW5zYWN0aW9uSWQiOiIxNjY4N2NjOS0zNDM5LTQxZTgtYTI3MC1lZDA2MmRhYTYzMjgiLCJxdW90ZUlkIjoiMWRmM2M3ZGYtOGIzZS00ZDM4LTk1NzktZWNjOGJlMTJiMWZiIiwicGF5ZWUiOnsicGFydHlJZEluZm8iOnsicGFydHlJZFR5cGUiOiJNU0lTRE4iLCJwYXJ0eUlkZW50aWZpZXIiOiIyNzcxMzgwMzkwNSIsImZzcElkIjoiMjEifSwicGVyc29uYWxJbmZvIjp7ImNvbXBsZXhOYW1lIjp7fX19LCJwYXllciI6eyJwYXJ0eUlkSW5mbyI6eyJwYXJ0eUlkVHlwZSI6Ik1TSVNETiIsInBhcnR5SWRlbnRpZmllciI6IjI1NjIwMTAwMDAxIiwiZnNwSWQiOiIyMCJ9LCJwZXJzb25hbEluZm8iOnsiY29tcGxleE5hbWUiOnsiZmlyc3ROYW1lIjoiTWF0cyIsImxhc3ROYW1lIjoiSGFnbWFuIn19fSwiYW1vdW50Ijp7ImN1cnJlbmN5IjoiVVNEIiwiYW1vdW50IjoiMTAwIn0sInRyYW5zYWN0aW9uVHlwZSI6eyJzY2VuYXJpbyI6IlRSQU5TRkVSIiwic3ViU2NlbmFyaW8iOiJUUkFOU0ZFUiIsImluaXRpYXRvciI6IlBBWUVSIiwiaW5pdGlhdG9yVHlwZSI6IkNPTlNVTUVSIiwicmVmdW5kSW5mbyI6e319LCJub3RlIjoiaGVqIn0=","secret".getBytes());
        //log.info("isMatching: "+ilpConditionHandlerImpl.validateFulfillmentAgainstCondition(fulfillment,"VA4oEYqWhtUrJmSAwa5xLuOIeKgo4ZFhRP4klOggz8c"));

        byte[] FULFILLMENT_PREFIX = new byte[]{(byte) 0xA0, 0x22, (byte) 0x80, 0x20};
        byte[] bDecodedCryptoCondFulfillment = java.util.Base64.getUrlDecoder().decode(rawFulfillment);
        byte[] bRawFulfillment = new byte[bDecodedCryptoCondFulfillment.length - FULFILLMENT_PREFIX.length];
        System.arraycopy(bDecodedCryptoCondFulfillment, FULFILLMENT_PREFIX.length, bRawFulfillment, 0, bRawFulfillment.length);
        String strRawFulfillment = java.util.Base64.getUrlEncoder().encodeToString(bRawFulfillment);

        log.info(strRawFulfillment.substring(0,strRawFulfillment.length()-1));

    }
}

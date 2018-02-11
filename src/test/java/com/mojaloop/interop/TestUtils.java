package com.mojaloop.interop;

import com.ilp.conditions.impl.IlpConditionHandlerImpl;
import io.restassured.path.json.JsonPath;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.Base64;
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
        //String rawFulfillment = ilpConditionHandlerImpl.generateFulfillment("AQAAAAAAAABkCnByaXZhdGUuMjGCAkl7InRyYW5zYWN0aW9uSWQiOiIxNjY4N2NjOS0zNDM5LTQxZTgtYTI3MC1lZDA2MmRhYTYzMjgiLCJxdW90ZUlkIjoiMWRmM2M3ZGYtOGIzZS00ZDM4LTk1NzktZWNjOGJlMTJiMWZiIiwicGF5ZWUiOnsicGFydHlJZEluZm8iOnsicGFydHlJZFR5cGUiOiJNU0lTRE4iLCJwYXJ0eUlkZW50aWZpZXIiOiIyNzcxMzgwMzkwNSIsImZzcElkIjoiMjEifSwicGVyc29uYWxJbmZvIjp7ImNvbXBsZXhOYW1lIjp7fX19LCJwYXllciI6eyJwYXJ0eUlkSW5mbyI6eyJwYXJ0eUlkVHlwZSI6Ik1TSVNETiIsInBhcnR5SWRlbnRpZmllciI6IjI1NjIwMTAwMDAxIiwiZnNwSWQiOiIyMCJ9LCJwZXJzb25hbEluZm8iOnsiY29tcGxleE5hbWUiOnsiZmlyc3ROYW1lIjoiTWF0cyIsImxhc3ROYW1lIjoiSGFnbWFuIn19fSwiYW1vdW50Ijp7ImN1cnJlbmN5IjoiVVNEIiwiYW1vdW50IjoiMTAwIn0sInRyYW5zYWN0aW9uVHlwZSI6eyJzY2VuYXJpbyI6IlRSQU5TRkVSIiwic3ViU2NlbmFyaW8iOiJUUkFOU0ZFUiIsImluaXRpYXRvciI6IlBBWUVSIiwiaW5pdGlhdG9yVHlwZSI6IkNPTlNVTUVSIiwicmVmdW5kSW5mbyI6e319LCJub3RlIjoiaGVqIn0=","secret".getBytes());
        //log.info("isMatching: "+ilpConditionHandlerImpl.validateFulfillmentAgainstCondition(fulfillment,"VA4oEYqWhtUrJmSAwa5xLuOIeKgo4ZFhRP4klOggz8c"));

        String rawFulfillment = "XgEQqmtuZXUNV_P7Xflh9FJaANzQkhQ7ue1uSPZSUA0";
        byte[] FULFILLMENT_PREFIX = new byte[]{(byte) 0xA0, 0x22, (byte) 0x80, 0x20};
        byte[] bDecodedFulfillment = Base64.getUrlDecoder().decode(rawFulfillment);
        byte[] newFulfillment = new byte[FULFILLMENT_PREFIX.length + bDecodedFulfillment.length];

        System.arraycopy(FULFILLMENT_PREFIX, 0, newFulfillment, 0, FULFILLMENT_PREFIX.length);
        System.arraycopy(bDecodedFulfillment, 0, newFulfillment, FULFILLMENT_PREFIX.length, bDecodedFulfillment.length);

        log.info(Base64.getUrlEncoder().encodeToString(newFulfillment));

        
    }

    public void testcreateDFSPGetResourcesResponse() {
        String data = "{\"party\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"27213971461\"},\"name\":\"alice cooper\",\"personalInfo\":{\"complexName\":{\"firstName\":\"alice\",\"lastName\":\"cooper\"},\"dateOfBirth\":\"1989-03-22\"}}}";
        Utils.createDFSPGetResourcesResponse(data,"dfsp1","localhost","8014");
    }

    public void testDFSPGetQuotesRequest(){
        String data = "{\"quoteId\":\"c61cf17c-0c60-4147-8124-8ebe4729ade5\",\"transactionId\":\"c61cf17c-0c60-4147-8124-8ebe4729ade5\",\"payer\":{\"partyIdInfo\":{\"partyIdentifier\":\"14614767\",\"partyIdType\":\"MSISDN\"}},\"payee\":{\"partyIdInfo\":{\"partyIdentifier\":\"27213971461\",\"partyIdType\":\"MSISDN\"}},\"amount\":{\"amount\":\"1200\",\"currency\":\"USD\"},\"amountType\":\"SEND\",\"transactionType\":{\"scenario\":\"DEPOSIT\",\"initiator\":\"PAYER\",\"initiatorType\":\"CONSUMER\"}}";
        Utils.createDFSPQuotesRequest(data);
    }
}

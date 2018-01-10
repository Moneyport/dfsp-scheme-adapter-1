package com.mojaloop.interop;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.logging.Logger;


public class TestUtils extends TestCase {

    Logger log = Logger.getLogger("TestUtils.class");

    public void testGetMojaloopPartiesResponse(){
        String testJson = "{\"type\":\"payee\",\"name\":\"bob dylan\",\"firstName\":\"bob\",\"lastName\":\"dylan\",\"nationalId\":\"123456789\",\"dob\":\"1999-12-10\",\"account\":\"http://ec2-52-32-130-4.us-west-2.compute.amazonaws.com:8014/ledger/accounts/bob\",\"currencyCode\":\"TZS\",\"currencySymbol\":\"TSh\",\"imageUrl\":\"https://red.ilpdemo.org/api/receivers/bob_dylan/profile_pic.jpg\"}";
        log.info("Response: "+Utils.getMojaloopPartiesResponse(testJson,"123456"));
    }
}

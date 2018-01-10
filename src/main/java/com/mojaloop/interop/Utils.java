package com.mojaloop.interop;

import org.mule.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Utils {

    public static HashMap<String,String> createAddParticipantRequest(String request, String authorization){

        HashMap<String,String> retMap = new HashMap<String,String>();

        JsonObject dataObject = Json.createReader(new StringReader(request)).readObject();
        String identifierWithType = dataObject.getString("identifier");
        String identifierType = identifierWithType.substring(0,identifierWithType.indexOf(":"));
        if(identifierType.equalsIgnoreCase("tel"))
            retMap.put("identifierType","MSISDN");
        else
            retMap.put("identifierType","ALIAS");
        String identifier = identifierWithType.substring(identifierWithType.indexOf(":")+1);
        retMap.put("identifier",identifier);
        boolean primary = dataObject.containsKey("primary");


        String base64Credentials = authorization.substring("Basic".length()).trim();
        String credentials = new String(Base64.decode(base64Credentials), Charset.forName(Base64.PREFERRED_ENCODING));
        String dfspName = credentials.split(":")[0];
        retMap.put("dfspName",dfspName);

        String addParticipantsRequest = Json.createObjectBuilder()
                .add("fspId", dfspName)
                .add("currency", "USD")
                .build()
                .toString();
        retMap.put("addParticipantsRequest",addParticipantsRequest);

        String resourceResponse = Json.createObjectBuilder()
                .add("name", dfspName)
                .add("shortName", dfspName)
                .add("providerUrl", "http://localhost:8089/dfsp/scheme/adapter/v1")
                .add("primary", String.valueOf(primary))
                .add("registered", String.valueOf(primary))
                .build()
                .toString();
        retMap.put("resourceResponse",resourceResponse);

        return retMap;
    }

    public static String getMojaloopPartiesResponse(String dfspResponse,String receiverId){
        JsonObject objdfspResponse = Json.createReader(new StringReader(dfspResponse)).readObject();

        return Json.createObjectBuilder().add("party",Json.createObjectBuilder()
                .add("partyIdInfo",
                        Json.createObjectBuilder().add("partyIdType","MSISDN").add("partyIdentifier",receiverId))
                .add("name",objdfspResponse.getString("name"))
                .add("personalInfo",
                        Json.createObjectBuilder().add("complexName",
                                Json.createObjectBuilder().add("firstName",objdfspResponse.getString("firstName"))
                                                            .add("lastName",objdfspResponse.getString("lastName"))
                        )
                        .add("dateOfBirth",objdfspResponse.getString("dob"))
                )
        ).build().toString();

    }
}

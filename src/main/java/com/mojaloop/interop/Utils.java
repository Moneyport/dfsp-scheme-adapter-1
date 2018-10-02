package com.mojaloop.interop;

import com.ilp.conditions.impl.IlpConditionHandlerImpl;
import com.ilp.conditions.models.pdp.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import io.restassured.path.json.JsonPath;
import org.apache.http.client.utils.DateUtils;
import org.jcodings.util.Hash;
import org.mule.util.Base64;
import org.mule.util.ExceptionUtils;
import org.mule.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;


public class Utils {

    private static Logger log = Logger.getLogger("Utils.class");

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

    public static String createMojaloopPartiesResponse(String dfspResponse,String receiverId){
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

    public static String createDFSPGetResourcesResponse(String mlPartiesResponse, String dfspName, String dfspHost,String dfspPort){
        try {
            log.info("In createDFSPGetResourcesResponse. mlPartiesResponse: "+mlPartiesResponse+" dfspHost: "+dfspHost+" dfspPort: "+dfspPort);
            JsonPath jPath = JsonPath.from(mlPartiesResponse);
            String fullName = jPath.getString("party.personalInfo.complexName.firstName") + " " + jPath.getString("party.personalInfo.complexName.lastName");
            String account = "http://" + dfspHost + ":" + dfspPort + "/accounts/" + jPath.getString("party.personalInfo.complexName.lastName");
            JsonObject dfspDetails = Json.createObjectBuilder()
                    .add("type", "payee")
                    .add("name", fullName)
                    .add("firstName", jPath.getString("party.personalInfo.complexName.firstName"))
                    .add("lastName", jPath.getString("party.personalInfo.complexName.lastName"))
                    .add("nationalId", "")
                    .add("dob", (jPath.getString("party.personalInfo.dateOfBirth")!=null) ? jPath.getString("party.personalInfo.dateOfBirth"):"")
                    .add("account", account)
                    .add("currencyCode", "TZS")
                    .add("currencySymbol", "TSh")
                    .add("imageUrl", "https://red.ilpdemo.org/api/receivers/alice_cooper/profile_pic.jpg")
                    .build();

            JsonObject fraudDetails = Json.createObjectBuilder()
                    .add("id", UUID.getUUID())
                    .add("score", 0)
                    .add("createdDate", DateUtils.formatDate(Calendar.getInstance().getTime(), "YYYY-MM-DD HH:mm:ss.SSSZ"))
                    .build();

            JsonArray directoryDetails = Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                            .add("name", dfspName)
                            .add("shortName", dfspName)
                            .add("providerUrl", "http://" + dfspHost + ":8088/scheme/adapter/v1")
                            .add("primary", true)
                            .add("registered", true)
                    )
                    .build();
            return Json.createObjectBuilder()
                    .add("dfsp_details", dfspDetails)
                    .add("fraud_details", fraudDetails)
                    .add("directory_details", directoryDetails)
                    .build()
                    .toString();

        }catch(Exception e){
            log.info("Exception in createDFSPGetResourcesResponse: "+ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }




    public static String createMojaloopQuotesRequest(String dfspQuotesRequest,String payerFspId, String payeeFspId){
        try {
            JsonPath jPath = JsonPath.from(dfspQuotesRequest);
            return Json.createObjectBuilder()
                            .add("quoteId",jPath.getString("paymentId"))
                            .add("transactionId",jPath.getString("paymentId"))
                            .add("payer", Json.createObjectBuilder()
                                    .add("partyIdInfo",Json.createObjectBuilder()
                                            .add("partyIdentifier",jPath.getString("payer.identifier"))
                                            .add("partyIdType","MSISDN")
                                            .add("fspId",payerFspId)
                                    )
                            )
                            .add("payee", Json.createObjectBuilder()
                                    .add("partyIdInfo",Json.createObjectBuilder()
                                            .add("partyIdentifier",jPath.getString("payee.identifier"))
                                            .add("partyIdType","MSISDN")
                                            .add("fspId",payeeFspId)
                                    )
                            )
                            .add("amount",Json.createObjectBuilder()
                                    .add("amount",jPath.getString("amount.amount"))
                                    .add("currency","USD")
                            )
                            .add("amountType","SEND")
                            .add("transactionType", Json.createObjectBuilder()
                                    .add("scenario","DEPOSIT")
                                    .add("initiator","PAYER")
                                    .add("initiatorType","CONSUMER")
                            )
                            .build()
                            .toString();

        } catch(Exception e){
            log.info("Error in createMojaloopQuotesRequest: "+ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public static String createDFSPQuotesRequest(String mojaloopQuotesRequest){
        try {
            com.jayway.jsonpath.DocumentContext ctx = com.jayway.jsonpath.JsonPath.parse(mojaloopQuotesRequest);
            return Json.createObjectBuilder()
                    .add("paymentId", ctx.read("quoteId").toString())
                    .add("payer", Json.createObjectBuilder()
                            .add("identifier", ctx.read("payer.partyIdInfo.partyIdentifier").toString())
                            .add("identifierType", "eur")
                    )
                    .add("payee", Json.createObjectBuilder()
                            .add("identifier", ctx.read("payee.partyIdInfo.partyIdentifier").toString())
                            .add("identifierType", "eur")
                            .add("account","http://host/ledger/accounts/alice")
                    )
                    .add("transferType", "p2p")
                    .add("amountType", "SEND")
                    .add("amount", Json.createObjectBuilder()
                            .add("amount", ctx.read("amount.amount").toString())
                            .add("currency", ctx.read("amount.currency").toString())
                    )
                    .build()
                    .toString();
        }catch (Exception e){
            log.info("Error in createDFSPQuotesRequest:"+ org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }

    public static String createMojaloopQuotesResponse(String dfspQuotesResponse,String originalMojaloopQuotesRequest) throws Exception {

        try {
            com.jayway.jsonpath.DocumentContext ctxDfspQuotesResponse = com.jayway.jsonpath.JsonPath.parse(dfspQuotesResponse);
            com.jayway.jsonpath.DocumentContext cxtOriginalMojaloopQuotesRequest = com.jayway.jsonpath.JsonPath.parse(originalMojaloopQuotesRequest, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS));

            //String fspId = jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.fspId");
            String amountStr = cxtOriginalMojaloopQuotesRequest.read("amount.amount");
            //String ilpAddress = "private.".concat(fspId);
            String ilpAddress = "private.".concat("dfsp2");
            long amount = (long) Double.parseDouble(amountStr);

            Transaction transaction = populateTransactionWithQuote(cxtOriginalMojaloopQuotesRequest);
            //Transaction transaction = new Transaction();

            log.info("IlpAddress: " + ilpAddress + " Amount: " + amount + " and Transaction: " + transaction.toString());

            //Call interop-ilp-conditions jar getIlpPacket()
            IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
            String ilpPacket = ilpConditionHandlerImpl.getILPPacket(ilpAddress, amount, transaction);

            //Call interop-ilp-conditions jar generateCondition()
            byte[] secret = "secret".getBytes();
            String ilpCondition = ilpConditionHandlerImpl.generateCondition(ilpPacket, secret);

            return Json.createObjectBuilder()
                    .add("transferAmount", Json.createObjectBuilder()
                            .add("amount", cxtOriginalMojaloopQuotesRequest.read("amount.amount").toString())
                            .add("currency", cxtOriginalMojaloopQuotesRequest.read("amount.currency").toString())
                    )
                    .add("payeeFspFee", Json.createObjectBuilder()
                            .add("amount", ctxDfspQuotesResponse.read("payeeFee.amount").toString())
                            .add("currency", ctxDfspQuotesResponse.read("payeeFee.currency").toString())
                    )
                    .add("payeeFspCommission", Json.createObjectBuilder()
                            .add("amount", ctxDfspQuotesResponse.read("payeeCommission.amount").toString())
                            .add("currency", ctxDfspQuotesResponse.read("payeeCommission.currency").toString())
                    )
                    .add("expiration", ctxDfspQuotesResponse.read("expiresAt").toString())
                    .add("ilpPacket", ilpPacket)
                    .add("condition", ilpCondition)
                    .build()
                    .toString();
        }catch (Exception e){
            log.info("Error in createMojaloopQuotesResponse: "+ org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }

    public static String createDFSPQuotesResponse(String mlQuotesResponse,String paymentId,String dfspName,String dfspHost) throws Exception{
        try {
            com.jayway.jsonpath.DocumentContext ctxMlQuotesResponse = com.jayway.jsonpath.JsonPath.parse(mlQuotesResponse);
            String rawIPR = "ILPPACKET:"+ctxMlQuotesResponse.read("ilpPacket").toString()+"ILPCONDITION:"+ctxMlQuotesResponse.read("condition").toString();
            String encodedIPR = Base64.encodeBytes(rawIPR.getBytes());
            return Json.createObjectBuilder()
                    .add("paymentId", paymentId)
                    .add("receiveAmount", Json.createObjectBuilder()
                            .add("amount", ctxMlQuotesResponse.read("transferAmount.amount").toString())
                            .add("currency", ctxMlQuotesResponse.read("transferAmount.currency").toString())
                    )
                    .add("payeeFee", Json.createObjectBuilder()
                            .add("amount", ctxMlQuotesResponse.read("payeeFspFee.amount").toString())
                            .add("currency", ctxMlQuotesResponse.read("payeeFspFee.currency").toString())
                    )
                    .add("payeeCommission", Json.createObjectBuilder()
                            .add("amount", ctxMlQuotesResponse.read("payeeFspCommission.amount").toString())
                            .add("currency", ctxMlQuotesResponse.read("payeeFspCommission.currency").toString())
                    )
                    .add("connectorAccount", "http://"+dfspHost+":8014/accounts/"+dfspName+"-testconnector")
                    .add("sourceExpiryDuration", "100")
                    .add("ipr", encodedIPR)
                    .build()
                    .toString();
        } catch(Exception e){
            log.info("Error in createDFSPQuotesResponse: "+ org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }



    public static HashMap extractDataFromMojaloopTransferRequest(String mojaloopTransferRequest) throws Exception{
        try {
            HashMap<String,String> retMap = new HashMap<String,String>();
            JsonPath jPathMojaloopTransferRequest = JsonPath.from(mojaloopTransferRequest);

            String ilpPacket = jPathMojaloopTransferRequest.getString("ilpPacket");
            IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
            Transaction decodedTxn = ilpConditionHandlerImpl.getTransactionFromIlpPacket(ilpPacket);
            log.info("PayerId: " + decodedTxn.getPayer().getPartyIdInfo().getPartyIdentifier());
            log.info("PayeeId: " + decodedTxn.getPayee().getPartyIdInfo().getPartyIdentifier());

            retMap.put("payerId",decodedTxn.getPayer().getPartyIdInfo().getPartyIdentifier());
            retMap.put("payeeId",decodedTxn.getPayee().getPartyIdInfo().getPartyIdentifier());

            retMap.put("transferId",jPathMojaloopTransferRequest.getString("transferId"));

            return retMap;

        }catch (Exception e){
            log.info("Error in extractDataFromMojaloopTransferRequest: "+ExceptionUtils.getStackTrace(e));
            throw e;
        }

    }

    public static HashMap<String, String> createDFSPPayerPrepareTransferRequest(String dfspRequest, String dfspHost) throws Exception {
        try {
            com.jayway.jsonpath.DocumentContext ctxDfspRequest = com.jayway.jsonpath.JsonPath.parse(dfspRequest);
            HashMap<String,String> retMap = new HashMap<String,String>();

            //Extract ilpPacket and ilpCondition
            String decodedIPR = new String(Base64.decode(ctxDfspRequest.read("ipr")));
            String ilpPacket = decodedIPR.substring(10,decodedIPR.indexOf("ILPCONDITION:"));
            log.info("ilpPacket: "+ilpPacket);
            retMap.put("ilpPacket",ilpPacket);
            String ilpCondition = decodedIPR.substring(decodedIPR.indexOf("ILPCONDITION:")+13);
            log.info("ilpCondition: "+ilpCondition);
            retMap.put("ilpCondition",ilpCondition);

            IlpConditionHandlerImpl ilpConditionHandler = new IlpConditionHandlerImpl();
            Transaction transaction = ilpConditionHandler.getTransactionFromIlpPacket(ilpPacket);
            String paymentId = transaction.getTransactionId();
            retMap.put("paymentId",paymentId);

            //Calculate Expiration time
            Date expiryDt = org.apache.commons.lang3.time.DateUtils.addHours(new Date(),10);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");


            String prepareRequest =  Json.createObjectBuilder()
                    .add("id", "http://" + dfspHost + ":8014" + "/ledger/transfers/" + paymentId)
                    .add("ledger", "http://" + dfspHost + ":8014" + "/ledger")
                    .add("execution_condition", "ni:///sha-256;" + ilpCondition + "?fpt=preimage-sha-256&cost=32")
                    .add("expires_at", sdf.format(expiryDt))
                    .add("debits", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("account", ctxDfspRequest.read("sourceAccount").toString())
                                    .add("amount", ctxDfspRequest.read("sourceAmount").toString())
                            )
                    )
                    .add("credits", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("account", ctxDfspRequest.read("connectorAccount").toString())
                                    .add("amount", ctxDfspRequest.read("sourceAmount").toString())
                            )
                    )
                    .build()
                    .toString();

            retMap.put("prepareRequest",prepareRequest);

            return retMap;

        } catch(Exception e){
            log.info("Error in createDFSPPayerPrepareTransferRequest: "+org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }

    public static HashMap<String,String> createMojaloopPrepareTransferRequest(String dfspRequest) throws Exception {
        try {
            com.jayway.jsonpath.DocumentContext ctxDfspRequest = com.jayway.jsonpath.JsonPath.parse(dfspRequest);
            HashMap<String,String> retMap = new HashMap<String, String>();

            //Extract ilpPacket and ilpCondition
            String decodedIPR = new String(Base64.decode(ctxDfspRequest.read("ipr")));
            String ilpPacket = decodedIPR.substring(10,decodedIPR.indexOf("ILPCONDITION:"));
            log.info("ilpPacket: "+ilpPacket);
            String ilpCondition = decodedIPR.substring(decodedIPR.indexOf("ILPCONDITION:")+13);
            log.info("ilpCondition: "+ilpCondition);

            IlpConditionHandlerImpl ilpConditionHandler = new IlpConditionHandlerImpl();
            Transaction transaction = ilpConditionHandler.getTransactionFromIlpPacket(ilpPacket);
            String paymentId = transaction.getTransactionId();

            //Calculate Expiration time
            Date expiryDt = org.apache.commons.lang3.time.DateUtils.addHours(new Date(),10);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            //Custom logic to strip off .00 from amounts. This is to bypass RAML validation
            String srcAmount = ctxDfspRequest.read("sourceAmount");
            if(srcAmount.contains(".00"))
                srcAmount = srcAmount.substring(0,srcAmount.indexOf(".00"));

            retMap.put("mlPrepareRequest", Json.createObjectBuilder()
                    .add("transferId", paymentId)
                    .add("payerFsp", transaction.getPayer().getPartyIdInfo().getFspId())
                    .add("payeeFsp", transaction.getPayee().getPartyIdInfo().getFspId())
                    .add("amount", Json.createObjectBuilder()
                                    .add("amount", srcAmount)
                                    .add("currency", "USD")
                    )
                    .add("expiration", sdf.format(expiryDt))
                    .add("ilpPacket",ilpPacket)
                    .add("condition",ilpCondition)
                    .build()
                    .toString()
            );

            retMap.put("payerFspId",transaction.getPayer().getPartyIdInfo().getFspId());
            retMap.put("payeeFspId",transaction.getPayee().getPartyIdInfo().getFspId());

            return retMap;

        } catch(Exception e){
            log.info("Error in createMojaloopPrepareTransferRequest: "+org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
    }

    public static String createDFSPPrepareTransferRequest(String originalMojaloopTransferRequest, String dfspPayeeAccount, String dfspHost, String dfspLedgerPort, String dfspConnectorAccountName){
        try {
            JsonPath jPathOriginalMojaloopTransferRequest = JsonPath.from(originalMojaloopTransferRequest);

            return Json.createObjectBuilder()
                    .add("id", "http://" + dfspHost + ":" + dfspLedgerPort + "/ledger/transfers/" + jPathOriginalMojaloopTransferRequest.getString("transferId"))
                    .add("ledger", "http://" + dfspHost + ":" + dfspLedgerPort + "/ledger")
                    .add("execution_condition", "ni:///sha-256;" + jPathOriginalMojaloopTransferRequest.getString("condition") + "?fpt=preimage-sha-256&cost=32")
                    .add("expires_at", jPathOriginalMojaloopTransferRequest.getString("expiration"))
                    .add("credits", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("account", dfspPayeeAccount)
                                    .add("amount", jPathOriginalMojaloopTransferRequest.getString("amount.amount"))
                            )
                    )
                    .add("debits", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("account", "http://" + dfspHost + ":" + dfspLedgerPort + "/ledger/accounts/" + dfspConnectorAccountName)
                                    .add("amount", jPathOriginalMojaloopTransferRequest.getString("amount.amount"))
                                    .add("authorized", true)
                            )
                    )
                    .build()
                    .toString();
        } catch (Exception e){
            log.info("Error in createDFSPTransferRequest: "+ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    public static String createDFSPFulfillTransferRequest(String originalMojaloopTransferRequest){
        JsonPath jPathOriginalMojaloopTransferRequest = JsonPath.from(originalMojaloopTransferRequest);
        String ilpPacket = jPathOriginalMojaloopTransferRequest.getString("ilpPacket");
        IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
        String rawFulfillment = ilpConditionHandlerImpl.generateFulfillment(ilpPacket,"secret".getBytes());

        byte[] FULFILLMENT_PREFIX = new byte[]{(byte) 0xA0, 0x22, (byte) 0x80, 0x20};
        byte[] bDecodedFulfillment = java.util.Base64.getUrlDecoder().decode(rawFulfillment);
        byte[] newFulfillment = new byte[FULFILLMENT_PREFIX.length + bDecodedFulfillment.length];

        System.arraycopy(FULFILLMENT_PREFIX, 0, newFulfillment, 0, FULFILLMENT_PREFIX.length);
        System.arraycopy(bDecodedFulfillment, 0, newFulfillment, FULFILLMENT_PREFIX.length, bDecodedFulfillment.length);

        return java.util.Base64.getUrlEncoder().encodeToString(newFulfillment);

    }

    public static String createMojaloopFulfillTransferResponse(String originalMojaloopTransferRequest){
        JsonPath jPathOriginalMojaloopTransferRequest = JsonPath.from(originalMojaloopTransferRequest);
        String ilpPacket = jPathOriginalMojaloopTransferRequest.getString("ilpPacket");
        IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
        String rawFulfillment = ilpConditionHandlerImpl.generateFulfillment(ilpPacket,"secret".getBytes());

        return Json.createObjectBuilder()
                        .add("fulfilment",rawFulfillment)
                        .add("transferState","COMMITTED")
                        .add("completedTimestamp","2018-10-30T08:38:08.699-04:00")
                        .build()
                        .toString();
    }

    public static String createPayerDFSPFulfillTransferRequest(String mojaloopFulfillRequest){
        JsonPath jPathOriginalMojaloopTransferRequest = JsonPath.from(mojaloopFulfillRequest);

        byte[] FULFILLMENT_PREFIX = new byte[]{(byte) 0xA0, 0x22, (byte) 0x80, 0x20};
        byte[] bDecodedFulfillment = java.util.Base64.getUrlDecoder().decode(jPathOriginalMojaloopTransferRequest.getString("fulfilment"));
        byte[] newFulfillment = new byte[FULFILLMENT_PREFIX.length + bDecodedFulfillment.length];

        System.arraycopy(FULFILLMENT_PREFIX, 0, newFulfillment, 0, FULFILLMENT_PREFIX.length);
        System.arraycopy(bDecodedFulfillment, 0, newFulfillment, FULFILLMENT_PREFIX.length, bDecodedFulfillment.length);

        return java.util.Base64.getUrlEncoder().encodeToString(newFulfillment);

    }

    public static String createPayerDFSPFulfillTransferResponse(String paymentId, String dfspRequest,String fulfillment){
        JsonPath jDfspRequest = JsonPath.from(dfspRequest);
        return Json.createObjectBuilder()
                .add("paymentId",paymentId)
                .add("connectorAccount",jDfspRequest.getString("connectorAccount"))
                .add("status","executed")
                .add("fulfillment",fulfillment)
                .build()
                .toString();
    }

    private static Transaction populateTransactionWithQuote(com.jayway.jsonpath.DocumentContext cxtOriginalMojaloopQuotesRequest) {

        Transaction transaction = new Transaction();

        transaction.setTransactionId(cxtOriginalMojaloopQuotesRequest.read("transactionId"));
        transaction.setQuoteId(cxtOriginalMojaloopQuotesRequest.read("quoteId"));

        //Populating Payer Info
        Party payer = new Party();
        payer.setMerchantClassificationCode(cxtOriginalMojaloopQuotesRequest.read("payer.merchantClassificationCode"));
        payer.setName(cxtOriginalMojaloopQuotesRequest.read("payer.name"));

        PartyIdInfo payerIdInfo = new PartyIdInfo();
        payerIdInfo.setPartyIdentifier(cxtOriginalMojaloopQuotesRequest.read("payer.partyIdInfo.partyIdentifier"));
        if(cxtOriginalMojaloopQuotesRequest.read("payer.partyIdInfo.partyIdType")!=null)
            payerIdInfo.setPartyIdType(cxtOriginalMojaloopQuotesRequest.read("payer.partyIdInfo.partyIdType"));
        else
            payerIdInfo.setPartyIdType("MSISDN");
        payerIdInfo.setPartySubIdOrType(cxtOriginalMojaloopQuotesRequest.read("payer.partyIdInfo.partySubIdOrType"));
        payerIdInfo.setFspId(cxtOriginalMojaloopQuotesRequest.read("payer.partyIdInfo.fspId"));
        payer.setPartyIdInfo(payerIdInfo);

        PartyPersonalInfo payerPersonalInfo  = new PartyPersonalInfo();
        PartyComplexName payerComplexName =  new PartyComplexName();
        payerComplexName.setFirstName(cxtOriginalMojaloopQuotesRequest.read("payer.personalInfo.complexName.firstName"));
        payerComplexName.setLastName(cxtOriginalMojaloopQuotesRequest.read("payer.personalInfo.complexName.lastName"));
        payerComplexName.setMiddleName(cxtOriginalMojaloopQuotesRequest.read("payer.personalInfo.complexName.middleName"));//TODO: Check for empty
        payerPersonalInfo.setComplexName(payerComplexName);
        payer.setPersonalInfo(payerPersonalInfo);
        transaction.setPayer(payer);

        //Populating Payee Info
        Party payee = new Party();
        payee.setMerchantClassificationCode(cxtOriginalMojaloopQuotesRequest.read("payee.merchantClassificationCode"));
        payee.setName(cxtOriginalMojaloopQuotesRequest.read("payee.name"));

        PartyIdInfo payeeIdInfo = new PartyIdInfo();
        payeeIdInfo.setPartyIdentifier(cxtOriginalMojaloopQuotesRequest.read("payee.partyIdInfo.partyIdentifier"));
        if(cxtOriginalMojaloopQuotesRequest.read("payee.partyIdInfo.partyIdType")!=null)
            payeeIdInfo.setPartyIdType(cxtOriginalMojaloopQuotesRequest.read("payee.partyIdInfo.partyIdType"));
        else
            payeeIdInfo.setPartyIdType("MSISDN");
        payeeIdInfo.setPartySubIdOrType(cxtOriginalMojaloopQuotesRequest.read("payee.partyIdInfo.partySubIdOrType"));
        payeeIdInfo.setFspId(cxtOriginalMojaloopQuotesRequest.read("payee.partyIdInfo.fspId"));
        payee.setPartyIdInfo(payeeIdInfo);

        PartyPersonalInfo payeePersonalInfo  = new PartyPersonalInfo();
        PartyComplexName payeeComplexName =  new PartyComplexName();
        payeeComplexName.setFirstName(cxtOriginalMojaloopQuotesRequest.read("payee.personalInfo.complexName.firstName"));
        payeeComplexName.setLastName(cxtOriginalMojaloopQuotesRequest.read("payee.personalInfo.complexName.lastName"));
        payeeComplexName.setMiddleName(cxtOriginalMojaloopQuotesRequest.read("payee.personalInfo.complexName.middleName"));
        payeePersonalInfo.setComplexName(payeeComplexName);
        payee.setPersonalInfo(payeePersonalInfo);
        transaction.setPayee(payee);

        Money m = new Money();
        m.setAmount(cxtOriginalMojaloopQuotesRequest.read("amount.amount"));
        m.setCurrency(cxtOriginalMojaloopQuotesRequest.read("amount.currency"));
        transaction.setAmount(m);

        TransactionType tt = new TransactionType();
        tt.setBalanceOfPayments(cxtOriginalMojaloopQuotesRequest.read("transactionType.balanceOfPayments"));
        tt.setInitiator(cxtOriginalMojaloopQuotesRequest.read("transactionType.initiator"));
        tt.setInitiatorType(cxtOriginalMojaloopQuotesRequest.read("transactionType.initiatorType"));
        Refund ri = new Refund();
        ri.setOriginalTransactionId(cxtOriginalMojaloopQuotesRequest.read("transactionType.refundInfo.originalTransactionId"));
        ri.setRefundReason(cxtOriginalMojaloopQuotesRequest.read("transactionType.refundInfo.refundReason"));
        tt.setRefundInfo(ri);
        tt.setScenario(cxtOriginalMojaloopQuotesRequest.read("transactionType.scenario"));
        tt.setSubScenario(cxtOriginalMojaloopQuotesRequest.read("transactionType.scenario"));
        transaction.setTransactionType(tt);

        transaction.setNote(cxtOriginalMojaloopQuotesRequest.read("note"));

//        ExtensionList el = new ExtensionList();
//        Extension e1 = new Extension();
//        e1.setKey(jPathOriginalMojaloopQuotesRequest.getString("extensionList[0].key"));
//        e1.setValue(jPathOriginalMojaloopQuotesRequest.getString("extensionList[0].value"));
//        Extension e2 = new Extension();
//        e2.setKey(jPathOriginalMojaloopQuotesRequest.getString("extensionList[1].key"));
//        e2.setValue(jPathOriginalMojaloopQuotesRequest.getString("extensionList[1].value"));
//        List<Extension> le = new ArrayList<>();
//        le.add(e1);
//        le.add(e2);
//        el.setExtension(le);
//        transaction.setExtensionList(el);

        return transaction;
    }

    public static String getDFSPName(String authorization){
        String base64Credentials = authorization.substring("Basic".length()).trim();
        String credentials = new String(Base64.decode(base64Credentials), Charset.forName(Base64.PREFERRED_ENCODING));
        log.info("Credentials: "+credentials);
        String dfspName = credentials.split(":")[0];
        log.info("dfspName: "+dfspName);

        return dfspName;
    }
}

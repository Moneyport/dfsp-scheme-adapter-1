package com.mojaloop.interop;

import com.ilp.conditions.impl.IlpConditionHandlerImpl;
import com.ilp.conditions.models.pdp.*;
import io.restassured.path.json.JsonPath;
import org.apache.http.client.utils.DateUtils;
import org.mule.util.Base64;
import org.mule.util.ExceptionUtils;
import org.mule.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Calendar;
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

    public static String createDFSPGetResourcesResponse(String mlPartiesResponse, String authorization,String dfspHost,String dfspPort){
        JsonObject objMLPartiesResponse = Json.createReader(new StringReader(mlPartiesResponse)).readObject();
        String fullName = objMLPartiesResponse.getString("party.personalInfo.complexName.firstName")+" "+ objMLPartiesResponse.getString("party.personalInfo.complexName.lastName");
        String account = "http://"+dfspHost+":"+dfspPort+"/accounts/"+objMLPartiesResponse.getString("party.personalInfo.complexName.lastName");

        JsonObject dfspDetails = Json.createObjectBuilder()
                                                    .add("type","payee")
                                                    .add("name",fullName)
                                                    .add("firstName",objMLPartiesResponse.getString("party.personalInfo.complexName.firstName"))
                                                    .add("lastName",objMLPartiesResponse.getString("party.personalInfo.complexName.lastName"))
                                                    .add("nationalId","")
                                                    .add("dob",objMLPartiesResponse.getString("party.personalInfo.dateOfBirth"))
                                                    .add("account",account)
                                                    .add("currencyCode","TZS")
                                                    .add("currencySymbol","TSh")
                                                    .add("imageUrl","https://red.ilpdemo.org/api/receivers/alice_cooper/profile_pic.jpg")
                                                    .build();

        JsonObject fraudDetails = Json.createObjectBuilder()
                                                    .add("id", UUID.getUUID())
                                                    .add("score",0)
                                                    .add("createdDate",DateUtils.formatDate(Calendar.getInstance().getTime(),"YYYY-MM-DDTHH:mm:ss.SSSZ"))
                                                    .build();

        JsonArray directoryDetails = Json.createArrayBuilder()
                                                    .add(Json.createObjectBuilder()
                                                                            .add("name",objMLPartiesResponse.getString("party.partyIdInfo.fspId"))
                                                                            .add("shortName",objMLPartiesResponse.getString("party.partyIdInfo.fspId"))
                                                                            .add("providerUrl","http://"+dfspHost+":8088/scheme/adapter/v1")
                                                                            .add("primary",true)
                                                                            .add("registered",true)
                                                    )
                                                    .build();
        return Json.createObjectBuilder()
                            .add("dfsp_details",dfspDetails)
                            .add("fraud_details",fraudDetails)
                            .add("directory_details",directoryDetails)
                            .build()
                            .toString();
    }


    public static String createDFSPQuotesRequest(String mojaloopQuotesRequest){
        try {
            JsonPath jPath = JsonPath.from(mojaloopQuotesRequest);
            return Json.createObjectBuilder()
                    .add("paymentId", jPath.getString("quoteId"))
                    .add("payer", Json.createObjectBuilder()
                            .add("identifier", jPath.getString("payer.partyIdInfo.partyIdentifier"))
                            .add("identifierType", "eur")
                    )
                    .add("payee", Json.createObjectBuilder()
                            .add("identifier", jPath.getString("payee.partyIdInfo.partyIdentifier"))
                            .add("identifierType", "eur")
                            .add("account","http://host/ledger/accounts/alice")
                    )
                    .add("transferType", "p2p")
                    .add("amountType", "SEND")
                    .add("amount", Json.createObjectBuilder()
                            .add("amount", jPath.getString("amount.amount"))
                            .add("currency", jPath.getString("amount.currency"))
                    )
                    .build()
                    .toString();
        }catch (Exception e){
            log.info("Error in createDFSPQuotesRequest:"+ ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    public static String createMojaloopQuotesResponse(String dfspQuotesResponse,String originalMojaloopQuotesRequest) throws Exception {

        JsonPath jPathDfspQuotesResponse = JsonPath.from(dfspQuotesResponse);
        JsonPath jPathOriginalMojaloopQuotesRequest = JsonPath.from(originalMojaloopQuotesRequest);

        String fspId = jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.fspId");
        String amountStr = jPathOriginalMojaloopQuotesRequest.getString("amount.amount");
        String ilpAddress = "private.".concat(fspId);
        long amount = (long)Double.parseDouble(amountStr);

        Transaction transaction = populateTransactionWithQuote(jPathOriginalMojaloopQuotesRequest);

        log.info("IlpAddress: " + ilpAddress + " Amount: " + amount + " and Transaction: " + transaction.toString());

        //Call interop-ilp-conditions jar getIlpPacket()
        IlpConditionHandlerImpl ilpConditionHandlerImpl = new IlpConditionHandlerImpl();
        String ilpPacket = ilpConditionHandlerImpl.getILPPacket(ilpAddress, amount, transaction);

        //Call interop-ilp-conditions jar generateCondition()
        byte[] secret = "secret".getBytes();
        String ilpCondition = ilpConditionHandlerImpl.generateCondition(ilpPacket, secret);

        return Json.createObjectBuilder()
                .add("transferAmount",Json.createObjectBuilder()
                                            .add("amount",jPathOriginalMojaloopQuotesRequest.getString("amount.amount"))
                                            .add("currency",jPathOriginalMojaloopQuotesRequest.getString("amount.currency"))
                )
                .add("payeeFspFee",Json.createObjectBuilder()
                                            .add("amount",jPathDfspQuotesResponse.getString("payeeFee.amount"))
                                            .add("currency",jPathDfspQuotesResponse.getString("payeeFee.currency"))
                )
                .add("payeeFspCommission",Json.createObjectBuilder()
                                            .add("amount",jPathDfspQuotesResponse.getString("payeeCommission.amount"))
                                            .add("currency",jPathDfspQuotesResponse.getString("payeeCommission.currency"))
                )
                .add("expiration",jPathDfspQuotesResponse.getString("expiresAt"))
                .add("ilpPacket",ilpPacket)
                .add("condition",ilpCondition)
                .build()
                .toString();

    }

    private static Transaction populateTransactionWithQuote(JsonPath jPathOriginalMojaloopQuotesRequest) {

        Transaction transaction = new Transaction();

        transaction.setTransactionId(jPathOriginalMojaloopQuotesRequest.getString("transactionId"));
        transaction.setQuoteId(jPathOriginalMojaloopQuotesRequest.getString("quoteId"));

        Party pe = new Party();
        pe.setMerchantClassificationCode(jPathOriginalMojaloopQuotesRequest.getString("payee.merchantClassificationCode"));
        pe.setName(jPathOriginalMojaloopQuotesRequest.getString("payee.name"));
        PartyIdInfo pii1 = new PartyIdInfo();
        pii1.setFspId(jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.fspId"));
        pii1.setPartyIdentifier(jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.partyIdentifier"));
        pii1.setPartyIdType(jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.partyIdType"));
        pii1.setPartySubIdOrType(jPathOriginalMojaloopQuotesRequest.getString("payee.partyIdInfo.partySubIdOrType"));
        pe.setPartyIdInfo(pii1);
        PartyPersonalInfo ppi1  = new PartyPersonalInfo();
        PartyComplexName pcn1 =  new PartyComplexName();
        pcn1.setFirstName(jPathOriginalMojaloopQuotesRequest.getString("payee.personalInfo.complexName.firstName"));
        pcn1.setLastName(jPathOriginalMojaloopQuotesRequest.getString("payee.personalInfo.complexName.lastName"));
        pcn1.setMiddleName(jPathOriginalMojaloopQuotesRequest.getString("payee.personalInfo.complexName.middleName"));//TODO: Check for empty
        ppi1.setComplexName(pcn1);
        pe.setPersonalInfo(ppi1);
        transaction.setPayee(pe);

        Party pr = new Party();
        pr.setMerchantClassificationCode(jPathOriginalMojaloopQuotesRequest.getString("payer.merchantClassificationCode"));
        pr.setName(jPathOriginalMojaloopQuotesRequest.getString("payer.name"));
        PartyIdInfo pii2 = new PartyIdInfo();
        pii2.setFspId(jPathOriginalMojaloopQuotesRequest.getString("payer.partyIdInfo.fspId"));
        pii2.setPartyIdentifier(jPathOriginalMojaloopQuotesRequest.getString("payer.partyIdInfo.partyIdentifier"));
        pii2.setPartyIdType(jPathOriginalMojaloopQuotesRequest.getString("payer.partyIdInfo.partyIdType"));
        pii2.setPartySubIdOrType(jPathOriginalMojaloopQuotesRequest.getString("payer.partyIdInfo.partySubIdOrType"));
        pr.setPartyIdInfo(pii2);
        PartyPersonalInfo ppi2  = new PartyPersonalInfo();
        PartyComplexName pcn2 =  new PartyComplexName();
        pcn2.setFirstName(jPathOriginalMojaloopQuotesRequest.getString("payer.personalInfo.complexName.firstName"));
        pcn2.setLastName(jPathOriginalMojaloopQuotesRequest.getString("payer.personalInfo.complexName.lastName"));
        pcn2.setMiddleName(jPathOriginalMojaloopQuotesRequest.getString("payer.personalInfo.complexName.middleName"));//TODO: Check for empty
        ppi2.setComplexName(pcn2);
        pr.setPersonalInfo(ppi2);
        transaction.setPayer(pr);

        Money m = new Money();
        m.setAmount(jPathOriginalMojaloopQuotesRequest.getString("amount.amount"));
        m.setCurrency(jPathOriginalMojaloopQuotesRequest.getString("amount.currency"));
        transaction.setAmount(m);

        TransactionType tt = new TransactionType();
        tt.setBalanceOfPayments(jPathOriginalMojaloopQuotesRequest.getString("transactionType.balanceOfPayments"));
        tt.setInitiator(jPathOriginalMojaloopQuotesRequest.getString("transactionType.initiator"));
        tt.setInitiatorType(jPathOriginalMojaloopQuotesRequest.getString("transactionType.initiatorType"));
        Refund ri = new Refund();
        ri.setOriginalTransactionId(jPathOriginalMojaloopQuotesRequest.getString("transactionType.refundInfo.originalTransactionId"));
        ri.setRefundReason(jPathOriginalMojaloopQuotesRequest.getString("transactionType.refundInfo.refundReason"));
        tt.setRefundInfo(ri);
        tt.setScenario(jPathOriginalMojaloopQuotesRequest.getString("transactionType.scenario"));
        tt.setSubScenario(jPathOriginalMojaloopQuotesRequest.getString("transactionType.scenario"));
        transaction.setTransactionType(tt);

        transaction.setNote(jPathOriginalMojaloopQuotesRequest.getString("note"));

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
                        .build()
                        .toString();
    }
}

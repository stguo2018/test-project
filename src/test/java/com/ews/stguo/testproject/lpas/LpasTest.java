package com.ews.stguo.testproject.lpas;

import com.ews.stguo.testproject.utils.TeslaSerializeUtil;
import com.ews.stguo.testproject.utils.client.ClientCreator;
import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.DisplayPrices;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.ExperimentMapEntry;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.LodgingPricingAvailabilityRequest;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.LodgingPricingAvailabilityResponse;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.MessageHeader;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.PriceFormat;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.ProductFilter;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.RatePlanType;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.Room;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.ShoppingPathType;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.SiteType;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LpasTest {

    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String GZIP = "gzip";
    public static final String LPAS_TESLA_VERSION = "Tesla-Version";
    public static final String LPAS_MSG_GUID = "Message-GUID";
    public static final String LPAS_TESLA_SCHEMA_HASH = "Tesla-Schema-Hash";
    public static final String LPAS_REQUEST_TYPE = "Request-Type";
    public static final String LPAS_CLIENT_ID = "Client-ID";

    @Test
    public void test01() throws Exception {
        WebClient webClient = ClientCreator.createWebClient();
        LodgingPricingAvailabilityRequest lpasRequest = buildLpasRequest();
        byte[] body = TeslaSerializeUtil.serializeEntity(lpasRequest, -6818968201939216140L);
        Map<String, String> headers = new HashMap<>();
        headers.put(LPAS_MSG_GUID, "172d915f-6ddf-4109-9593-61639c794496");
        headers.put(LPAS_CLIENT_ID, "EWS.LPP.Google.Expedia");
        headers.put(LPAS_REQUEST_TYPE, "1001");
        headers.put(LPAS_TESLA_VERSION, "2");
        headers.put(LPAS_TESLA_SCHEMA_HASH, "-6818968201939216140");
        byte[] responseBytes = webClient.post()
                .uri("https://localhost:5401/lpas")
//                .uri("http://localhost:8080/ews-spoofer/replay/lpas")
                .headers(httpHeaders -> headers.forEach((k, v) -> httpHeaders.put(k,
                        Collections.singletonList(v))))
                .syncBody(body)
                .exchange()
                .flatMap(response -> response.bodyToMono(byte[].class))
                .block();
        LodgingPricingAvailabilityResponse lpasResponse = TeslaSerializeUtil.deserializeResponse(responseBytes,
                -6818968201939216140L);
        String s = TeslaSerializeUtil.serializeJson(lpasResponse, -6818968201939216140L);
        try (BufferedWriter writer = RWFileUtils.getWriter("response.json")) {
            writer.write(s);
            writer.newLine();
            writer.flush();
        }
    }

    private LodgingPricingAvailabilityRequest buildLpasRequest() {
        LodgingPricingAvailabilityRequest req = new LodgingPricingAvailabilityRequest();

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMessageUUID("172d915f-6ddf-4109-9593-61639c794496");
        messageHeader.setClientID("EWS.LPP.Google.Expedia");
        messageHeader.setMessageName("LodgingPricingAvailabilityRequest");
        messageHeader.setMessageVersion("26.0");
        messageHeader.setMessageDateTime("2022-04-06T15:51:42.094+0800");
        req.setMessageHeader(messageHeader);

//        req.setHotelIDList(Arrays.asList(1443));
        req.setHotelIDList(Arrays.asList(20230,1443,2163007));
//        req.setHotelIDList(Arrays.asList(20230,1443,2163007,48895422,70309883,39758503,10033104,18360908,1866711,66090450,4258919,6437931,11064159,72064431,63906190,14737453,7737382,9080282,27204724,55262781,20103726,4434336,68728190,15153416,15549155,15591938,19933499,9623470,1566581,18363868,12705969,57735531,10607394,12537391,17452428,73665607,51306315,66445311,6644,19006242,9105267,14683778,893114,44341472,13365968,27035504,57410442,1457741,12361,5374101,12511176,8068435,2745789,4973256,74691577,79057,1640111,31156042,32445006,66967707,21047637,6173838,5006506,25090586,68099650,75816491,33447924,20156602,3264347,20156943,4264720,198931,74638122,528309,33892861));
//        req.setHotelIDList(Arrays.asList(20230,1443,2163007,48895422,70309883,39758503,10033104,18360908,1866711,66090450,4258919,6437931,11064159,72064431,63906190,14737453,7737382,9080282,27204724,55262781,20103726,4434336,68728190,15153416,15549155,15591938,19933499,9623470,1566581,18363868,12705969,57735531,10607394,12537391,17452428,73665607,51306315,66445311,6644,19006242,9105267,14683778,893114,44341472,13365968,27035504,57410442,1457741,12361,5374101,12511176,8068435,2745789,4973256,74691577,79057,1640111,31156042,32445006,66967707,21047637,6173838,5006506,25090586,68099650,75816491,33447924,20156602,3264347,20156943,4264720,198931,74638122,528309,33892861,30064031,11689225,30064033,30064034,34693440,33139635,34693441,30064037,42461896,30064039,34693444,33139639,32056437,995926,36685844,34693448,34693449,46380811,27570712,49284604,30943680,52657571,42529013,65702461,71467672,52657573,46380819,65702463,65702464,52657577,42529019,13077192,72548219,74107986,30235851,64175790,2076934,33139644,74107989,64175792,33139645,30064047,33139647,34693454,34693456,34693457,34693458,36685855,34693459,27570720,27570722));
//        req.setHotelIDList(Arrays.asList(20230,1443,2163007,48895422,70309883,39758503,10033104,18360908,1866711,66090450,4258919,6437931,11064159,72064431,63906190,14737453,7737382,9080282,27204724,55262781,20103726,4434336,68728190,15153416,15549155,15591938,19933499,9623470,1566581,18363868,12705969,57735531,10607394,12537391,17452428,73665607,51306315,66445311,6644,19006242,9105267,14683778,893114,44341472,13365968,27035504,57410442,1457741,12361,5374101,12511176,8068435,2745789,4973256,74691577,79057,1640111,31156042,32445006,66967707,21047637,6173838,5006506,25090586,68099650,75816491,33447924,20156602,3264347,20156943,4264720,198931,74638122,528309,33892861,7770279,12021189,4683449,66441229,788033,75858570,28823376,6198140,53418707,42313649,41477,38092634,51678730,32122551,3935957,14531,70122212,32107187,42630197,520848,20179424,34941312,72290189,1080619,202537,46356409,6251042,891021,6046,18547451,69390006,1059110,11963078,10517193,35136806,37107402,26556564,16339478,15749350,46352313,11994365,14864210,26989632,32493119,26555822,480835,55442901,9983401,39523863,24065897));

        Room room = new Room();
        room.setNumberOfAdults((byte) 2);
        req.setRoomList(Arrays.asList(room));

        req.setCheckin("2022-05-17");
        req.setCheckout("2022-05-24");

        req.setPointOfSale(1);
        req.setPartner(40451);
        req.setTravelerCompanyID(0);
        req.setCurrency("USD");
        req.setLanguage("en-US");
        req.setPointOfSaleCountryCode("US");

        req.setMarketingChannelID(10);
//        req.setMarketingChannelID(30010);
//        req.setSiteType(SiteType.Default);
        req.setSiteType(SiteType.MobileSite);
        req.setShoppingPath(ShoppingPathType.Standalone);
//        req.setShoppingPath(ShoppingPathType.Package);
        req.setProductFilters(Arrays.asList(ProductFilter.FullyAvailable));
//        req.setProductFilters(Arrays.asList(ProductFilter.CheapestProduct));
        req.setRatePlanTypes(Arrays.asList(RatePlanType.Standalone));
//        req.setRatePlanTypes(Arrays.asList(RatePlanType.Package));
//        req.setBusinessModels(Arrays.asList(BusinessModel.ExpediaCollect));

        ExperimentMapEntry experimentMapEntry = new ExperimentMapEntry();
        experimentMapEntry.setExperimentNameID(6);
        experimentMapEntry.setExperimentID(11796);
        experimentMapEntry.setBucketID(1);

        ExperimentMapEntry experimentMapEntry2 = new ExperimentMapEntry();
        experimentMapEntry.setExperimentNameID(15826);
        experimentMapEntry.setExperimentID(15826);
        experimentMapEntry.setBucketID(1);

        ExperimentMapEntry experimentMapEntry3 = new ExperimentMapEntry();
        experimentMapEntry.setExperimentNameID(12791);
        experimentMapEntry.setExperimentID(12791);
        experimentMapEntry.setBucketID(1);
        req.setExperimentMap(Arrays.asList(experimentMapEntry));
//        req.setExperimentMap(Arrays.asList(experimentMapEntry, experimentMapEntry2, experimentMapEntry3));

        PriceFormat priceFormat = new PriceFormat();
        priceFormat.setDisplayPrices(Arrays.asList(DisplayPrices.SummaryDisplayPrices, DisplayPrices.DetailedDisplayPrices));
        priceFormat.setCost(true);
        req.setPriceFormat(priceFormat);
        req.setReturnProviderDebugInfo(false);
        req.setRatePlanCount(0);
        req.setMaxHotelsAvail(9999);
        req.setInventoryProviderIDs(Arrays.asList(24,84));
        req.setReturnProviderDebugInfo(false);
        return req;
    }

}

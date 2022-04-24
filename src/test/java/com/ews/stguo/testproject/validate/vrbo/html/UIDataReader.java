package com.ews.stguo.testproject.validate.vrbo.html;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class UIDataReader {

    private static final Pattern PATTERN = Pattern.compile("^.*window.__INITIAL_STATE__ = (.*);$", Pattern.MULTILINE);

    private final WebClient webClient;

    public UIDataReader() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    }

    public UIData getUIData(String url) throws Exception {
        HtmlPage page = webClient.getPage(url);
        if (page != null) {
            return parseToUIData(page.asXml());
        }
        return null;
    }

    private UIData parseToUIData(String pageHtml) throws Exception {
        UIData data = new UIData();
        JSONObject dataObj = getPageData(pageHtml);
        updateSummary(data, dataObj);
        return data;
    }

    private JSONObject getPageData(String pageHtml) throws Exception {
        String dataLine = null;
        try (BufferedReader br = new BufferedReader(new StringReader(pageHtml))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("window.__INITIAL_STATE__ = ")) {
                    dataLine = line;
                    break;
                }
            }
        }
        String dataJson = null;
        if (StringUtils.isNotBlank(dataLine)) {
            Matcher matcher = PATTERN.matcher(dataLine);
            while (matcher.find()) {
                dataJson = matcher.group(1);
            }
        }
        return new JSONObject(dataJson);
    }

    private void updateSummary(UIData data, JSONObject jsonObj) {
        JSONObject listingReducer = jsonObj.optJSONObject("listingReducer");
        Optional.ofNullable(listingReducer).ifPresent(lr -> data.setPropertyType(lr.optString("propertyType")));
        Optional.ofNullable(listingReducer).ifPresent(lr -> data.setPropertyName(lr.optString("headline")));

        Optional<JSONObject> priceSummary = Optional.ofNullable(listingReducer).map(lr -> lr.optJSONObject("priceSummary"));
        priceSummary.ifPresent(ps -> data.setReferencePriceCurrency(ps.optString("currency")));
        priceSummary.ifPresent(ps -> data.setReferencePriceValue(String.valueOf(ps.optDouble("amount"))));

        Optional<JSONObject> address = Optional.ofNullable(listingReducer).map(lr -> lr.optJSONObject("address"));
        address.ifPresent(ad -> data.setCity(ad.optString("city")));
        address.ifPresent(ad -> data.setCountry(ad.optString("country")));
        address.ifPresent(ad -> data.setPostalCode(ad.optString("postalCode")));
        address.ifPresent(ad -> data.setProvince(ad.optString("stateProvince")));

        Optional<JSONObject> geoCode = Optional.ofNullable(listingReducer).map(lr -> lr.optJSONObject("geoCode"));
        geoCode.ifPresent(gc -> data.setGeoLatitude(gc.optString("latitude")));
        geoCode.ifPresent(gc -> data.setGeoLongitude(gc.optString("longitude")));
        geoCode.ifPresent(gc -> data.setObfuscated(gc.optBoolean("exact")));

        JSONObject reviewsReducer = jsonObj.optJSONObject("reviewsReducer");
        Optional.ofNullable(reviewsReducer).ifPresent(rr -> data.setVrboAvgRating(String.valueOf(rr.optInt("averageRating"))));
    }

    public static void main(String[] args) throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        HtmlPage page = null;
        try {
//            page = webClient.getPage("https://www.vrbo.com/100092ha?adultsCount=2&arrival=2021-08-02&departure=2021-08-06&unitId=318363");
//            page = webClient.getPage("https://www.vrbo.com/1517563?adultsCount=2&arrival=2021-08-02&departure=2021-08-06&unitId=2076359");
//            page = webClient.getPage("https://www.vrbo.com/8753243ha?adultsCount=2&arrival=2021-08-02&departure=2021-08-06&unitId=4816173");
            page = webClient.getPage("https://www.vrbo.com/8467360ha?adultsCount=2&arrival=2021-08-02&departure=2021-08-06&unitId=4525491");
        } finally {
            webClient.close();
        }
        String pageXml = "";
        if (page != null) {
            pageXml = page.asXml();
        }
//        try (BufferedWriter bw = RWFileUtils.getWriter("vrbo3.html")) {
//            bw.write(pageXml);
//            bw.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String dataLine = null;
        try (BufferedReader br = new BufferedReader(new StringReader(pageXml))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("window.__INITIAL_STATE__ = ")) {
                    dataLine = line;
                    break;
                }
            }
        }

        String dataJson = null;
        if (StringUtils.isNotBlank(dataLine)) {
            Matcher matcher = PATTERN.matcher(dataLine);
            while (matcher.find()) {
                dataJson = matcher.group(1);
            }
        }

        if (StringUtils.isNotBlank(dataJson)) {
            try (BufferedWriter bw = RWFileUtils.getWriter("vrbo.json")) {
                bw.write(dataJson);
                bw.flush();
            }
        }

        JSONObject jsonObj = new JSONObject(dataJson);
        System.out.println(jsonObj.optJSONObject("listingReducer").optString("propertyType"));
    }

}

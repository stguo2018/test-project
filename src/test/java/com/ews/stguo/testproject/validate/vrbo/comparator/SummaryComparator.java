package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.utils.controlfile.ControlFileRWUtils;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.PropertyId;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GeoLocation;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GuestRating;
import com.ews.stguo.testproject.validate.vrbo.model.summary.PropertyType;
import com.ews.stguo.testproject.validate.vrbo.model.summary.SummaryModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS1;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS2;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.COUNTRY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLATITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLONGITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONOBFUSCATED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.POSTALCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPEID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPENAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROVINCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOAVGRATING;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SummaryComparator extends VrboComparator<SummaryModel> {

    private Map<String, String> countryCodeMapping;
    private Map<String, String> countryNameMapping;
    private Map<String, String> provinceNameMapping;

    public SummaryComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping,
                             Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
        try {
            countryCodeMapping = new HashMap<>();
            ControlFileRWUtils.loadHotelIdStrByPaths(true,
                    "E:\\ews-29840\\CountryCodeMapping.csv")
                    .stream().map(line -> line.split(","))
                    .forEach(cs -> countryCodeMapping.put(cs[1], cs[0]));
            countryNameMapping = new HashMap<>();
            ControlFileRWUtils.loadHotelIdStrByPaths(true,
                    "E:\\ews-29840\\CountryNameMapping.csv")
                    .stream().map(line -> line.split(","))
                    .forEach(cs -> countryNameMapping.put(cs[1], cs[0]));
            provinceNameMapping = new HashMap<>();
            ControlFileRWUtils.loadHotelIdStrByPaths(true,
                    "E:\\ews-29840\\ProvinceNameMapping.csv")
                    .stream().map(line -> line.split(","))
                    .forEach(cs -> provinceNameMapping.put(cs[0], cs[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFileType() {
        return "Summary";
    }

    @Override
    public Class<SummaryModel> getClazz() {
        return SummaryModel.class;
    }

    @Override
    public String getId(SummaryModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(SummaryModel model, Optional<JSONObject> response) {
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });

        String propertyType = unit.map(u -> u.optString("propertyType"))
                .map(TrimUtils::trim).orElse(null);
        if (propertyType == null && (!Optional.ofNullable(model.getPropertyType()).map(PropertyType::getName).isPresent() ||
                model.getPropertyType().getName().equals(""))) {
            updateCounter(PROPERTYTYPEID);
            updateCounter(PROPERTYTYPENAME);
        } else if (StringUtils.isNotBlank(model.getPropertyType().getName()) && StringUtils.equalsIgnoreCase(model.getPropertyType().getName(), propertyType)) {
            updateCounter(PROPERTYTYPEID);
            updateCounter(PROPERTYTYPENAME);
        } else {
            updateNoMatchData(PROPERTYTYPENAME, model, StringUtils.isBlank(propertyType) ? "null" : propertyType);
        }

        Optional<JSONObject> reviewSummary = unit.map(u -> u.optJSONObject("reviewSummary"));
        BigDecimal avgRating = reviewSummary.map(rs -> rs.optDouble("averageRating")).map(String::valueOf)
                .map(BigDecimal::new).orElse(null);
        String fAvgRating = Optional.ofNullable(model.getGuestRating()).map(GuestRating::getVrbo).map(GuestRating.Rating::getAvgRating).orElse(null);
        if (avgRating == null && fAvgRating == null) {
            updateCounter(VRBOAVGRATING);
        } else if (avgRating != null && fAvgRating != null && avgRating.abs().subtract(new BigDecimal(fAvgRating).abs()).abs()
                .compareTo(new BigDecimal("0.000001")) <= 0) {
            updateCounter(VRBOAVGRATING);
        } else {
            updateNoMatchData(VRBOAVGRATING, model, avgRating == null ? "null" : String.valueOf(avgRating));
        }

        Optional<JSONObject> adContent = response.map(rs -> rs.optJSONObject("adContent"));
        Optional<JSONObject> headline = adContent.map(ac -> ac.optJSONObject("headline"));
        List<String> locales = new ArrayList<>();
        String propertyName = headline.map(hl -> hl.optJSONArray("texts")).map(ts -> {
            if (ts.length() > 0) {
                for (int i = 0; i < ts.length(); i++) {
                    Optional<JSONObject> text = Optional.ofNullable(ts.optJSONObject(i));
                    String locale = text.map(t -> t.optString("locale")).map(TrimUtils::trim).orElse(null);
                    // locales.add(locale);
                    if ("en".equalsIgnoreCase(locale)) {
                        return text.map(t -> t.optString("content")).map(TrimUtils::trim).orElse(null);
                    }
                }
            }
            return null;
        }).orElse(null);
        Optional<JSONObject> description = adContent.map(ac -> ac.optJSONObject("description"));
        String propertyDescription = description.map(ds -> ds.optJSONArray("texts")).map(ts -> {
            if (ts.length() > 0) {
                for (int i = 0; i < ts.length(); i++) {
                    Optional<JSONObject> text = Optional.ofNullable(ts.optJSONObject(i));
                    String locale = text.map(t -> t.optString("locale")).orElse(null);
                    locales.add(locale);
                    if ("en".equalsIgnoreCase(locale)) {
                        return text.map(t -> t.optString("content")).orElse(null);
                    }
                }
            }
            return null;
        }).orElse(null);
        if (CollectionUtils.isNotEmpty(locales)) {
            updateNoMatchData("Locales", model, String.join(",", locales));
        }

        if (StringUtils.equalsIgnoreCase(propertyName, model.getPropertyName()) ||
                (propertyName == null && model.getPropertyName().equals(""))) {
            updateCounter(PROPERTYNAME);
        } else {
            updateNoMatchData(PROPERTYNAME, model, StringUtils.isBlank(propertyName) ? "null" : propertyName);
        }

        Optional<JSONObject> location = response.map(rs -> rs.optJSONObject("location"));
        Optional<JSONObject> address = location.map(l -> l.optJSONObject("address"));
        String address1 = address.map(a -> a.optString("address1")).map(TrimUtils::trim).orElse(null);
        String address2 = address.map(a -> a.optString("address2")).map(TrimUtils::trim).orElse(null);
        String city = address.map(a -> a.optString("city")).map(TrimUtils::trim).orElse(null);
        String province = address.map(a -> a.optString("stateProvince")).map(TrimUtils::trim).orElse(null);
        String country = address.map(a -> a.optString("country")).map(TrimUtils::trim).orElse(null);
        String postalCode = address.map(a -> a.optString("postalCode")).map(TrimUtils::trim).orElse(null);
        if (StringUtils.equalsIgnoreCase(address1, model.getAddress1()) || (address1 == null && model.getAddress1().equals(""))) {
            updateCounter(ADDRESS1);
        } else {
            updateNoMatchData(ADDRESS1, model, StringUtils.isBlank(address1) ? "null" : address1);
        }
        if ((address2 == null && model.getAddress2() == null) || StringUtils.equalsIgnoreCase(address2, model.getAddress2()) || (address2 == null && model.getAddress2().equals(""))) {
            updateCounter(ADDRESS2);
        } else {
            updateNoMatchData(ADDRESS2, model, StringUtils.isBlank(address2) ? "null" : address2);
        }
        if ((city == null && model.getCity().equals("")) || (city != null && StringUtils.equalsIgnoreCase(city, model.getCity()))) {
            updateCounter(CITY);
        } else {
            updateNoMatchData(CITY, model, StringUtils.isBlank(city) ? "null" : city);
        }
        String c = StringUtils.isBlank(model.getCountry()) ? "null" : model.getCountry();
        String p = StringUtils.isBlank(province) ? "null" : province;
        String p2 = getProvinceName(c, p);
        province = StringUtils.equals("null", p2) ? p : p2;
        if (StringUtils.equalsIgnoreCase(province, model.getProvince()) || StringUtils.equals("null", province)) {
            updateCounter(PROVINCE);
        } else {
            updateNoMatchData(PROVINCE, model, c + ";;" + p);
        }
        if ((country == null && model.getCountry() == null) || (country == null && model.getCountry().equals(""))) {
            updateCounter(COUNTRY);
        } else if (country != null && model.getCountry() != null && model.getCountry().equalsIgnoreCase(new Locale("", country).getDisplayCountry())) {
            updateCounter(COUNTRY);
        } else {
            updateNoMatchData(COUNTRY, model, StringUtils.isBlank(country) ? "null" : country);
        }
        if (StringUtils.equalsIgnoreCase(postalCode, model.getPostalCode()) || (postalCode == null && model.getPostalCode().equals(""))) {
            updateCounter(POSTALCODE);
        } else {
            updateNoMatchData(POSTALCODE, model, StringUtils.isBlank(postalCode)  ? "null" : postalCode);
        }

        Optional<JSONObject> geoLocation = location.map(l -> l.optJSONObject("latLng"));
        Boolean showExactLocation = location.map(l -> l.optBoolean("showExactLocation")).orElse(null);
        Boolean obfuscated = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::isObfuscated).orElse(null);
        if (showExactLocation == null && obfuscated == null) {
            updateCounter(GEOLOCATIONOBFUSCATED);
        } else if (showExactLocation != null && obfuscated != null && !showExactLocation == obfuscated) {
            updateCounter(GEOLOCATIONOBFUSCATED);
        } else {
//            updateNoMatchData(GEOLOCATIONOBFUSCATED, model, showExactLocation == null ? "null" : String.valueOf(!showExactLocation));
        }
        if (showExactLocation != null && !showExactLocation) {
            updateNoMatchData(GEOLOCATIONOBFUSCATED, model, showExactLocation == null ? "null" : String.valueOf(!showExactLocation));
        }
        Double latitude = geoLocation.map(gl -> gl.optDouble("latitude")).orElse(null);
        Double longitude = geoLocation.map(gl -> gl.optDouble("longitude")).orElse(null);
        String fLatitude = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::getLatitude).orElse(null);
        String fLongitude = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::getLongitude).orElse(null);
        if (latitude == null && fLatitude == null) {
            updateCounter(GEOLOCATIONLATITUDE);
        } else if (latitude != null && fLatitude != null && new BigDecimal(String.valueOf(latitude)).abs().subtract(new BigDecimal(fLatitude).abs()).abs()
                .compareTo(new BigDecimal("0.000001")) <= 0) {
            updateCounter(GEOLOCATIONLATITUDE);
        } else {
             updateNoMatchData(GEOLOCATIONLATITUDE, model, String.valueOf(latitude));
        }
        if (longitude == null && fLongitude == null) {
            updateCounter(GEOLOCATIONLONGITUDE);
        } else if (longitude != null && fLongitude != null && new BigDecimal(String.valueOf(longitude)).abs().subtract(new BigDecimal(fLongitude).abs()).abs()
                .compareTo(new BigDecimal("0.000001")) <= 0) {
            updateCounter(GEOLOCATIONLONGITUDE);
        } else {
            updateNoMatchData(GEOLOCATIONLONGITUDE, model, String.valueOf(longitude));
        }
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(/*PROPERTYTYPENAME, VRBOAVGRATING, PROPERTYNAME, ADDRESS1,
                ADDRESS2, CITY, PROVINCE, COUNTRY, POSTALCODE, GEOLOCATIONLATITUDE, GEOLOCATIONLONGITUDE,
                GEOLOCATIONOBFUSCATED,*/ "Locales");
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<SummaryModel>> noMatchData = getNoMatchData(type);
        if (type.equals("Locales")) {
            Map<String, Integer> count = new HashMap<>();
            AtomicInteger totalSize = new AtomicInteger(0);
            noMatchData.forEach((k, v) -> {
                totalSize.set(totalSize.get() + v.size());
                List<String> locales = Arrays.asList(k.split(","));
                locales.forEach(l -> {
                    if (count.containsKey(l)) {
                        count.put(l, count.get(l) + v.size());
                    } else {
                        count.put(l, v.size());
                    }
                });
            });
            count.forEach((k, v) -> {
                BigDecimal vv = new BigDecimal(String.valueOf(v*100)).divide(new BigDecimal(String.valueOf(totalSize.get())), 3, RoundingMode.HALF_UP);
                System.out.println("Locale: " + k + ", Number: (" + v + "/" + totalSize.get() + "), Percentage: " + vv + "%");
            });
        }
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, SummaryModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<SummaryModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<SummaryModel>> entry : noMatchData.entrySet()) {
            for (SummaryModel summaryModel : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, summaryModel, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(summaryModel.getPropertyId().getExpedia(), "false")
                        + getKey(type, summaryModel) + entry.getKey();
                List<Pair<String, SummaryModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), summaryModel));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, SummaryModel>>> entry : specialGroups.entrySet()) {
            for (Pair<String, SummaryModel> p : entry.getValue()) {
                SummaryModel summaryModel = p.getRight();
                row = sheet2.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, summaryModel, p.getLeft(), entry.getValue().size());
                }
                break;
            }
        }

        try (FileOutputStream out = new FileOutputStream(getFilePath(type))) {
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(String type) {
        String filePath = "E:\\ews-29840\\Summary\\";
        switch (type) {
            case PROVINCE:
                filePath += "Summary-Province.xlsx";
                break;
            case GEOLOCATIONLATITUDE:
                filePath += "Summary-GeoLatitude.xlsx";
                break;
            case GEOLOCATIONLONGITUDE:
                filePath += "Summary-GeoLongitude.xlsx";
                break;
            case GEOLOCATIONOBFUSCATED:
                filePath += "Summary-GeoObfuscated.xlsx";
                break;
            case POSTALCODE:
                filePath += "Summary-PostalCode.xlsx";
                break;
            case COUNTRY:
                filePath += "Summary-Country.xlsx";
                break;
            case CITY:
                filePath += "Summary-City.xlsx";
                break;
            case ADDRESS1:
                filePath += "Summary-Address1.xlsx";
                break;
            case ADDRESS2:
                filePath += "Summary-Address2.xlsx";
                break;
            case PROPERTYNAME:
                filePath += "Summary-PropertyName.xlsx";
                break;
            case VRBOAVGRATING:
                filePath += "Summary-AvgRating.xlsx";
                break;
            case PROPERTYTYPENAME:
                filePath += "Summary-PropertyType.xlsx";
                break;
            case "Locales":
                filePath += "Locales.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, SummaryModel summaryModel) {
        String key = null;
        switch (type) {
            case PROVINCE:
                key = summaryModel.getProvince() == null ? "null" : summaryModel.getProvince();
                break;
            case GEOLOCATIONLATITUDE:
                key = summaryModel.getGeoLocation() == null ? "null" : summaryModel.getGeoLocation().getLatitude();
                break;
            case GEOLOCATIONLONGITUDE:
                key = summaryModel.getGeoLocation() == null ? "null" : summaryModel.getGeoLocation().getLongitude();
                break;
            case GEOLOCATIONOBFUSCATED:
                key = summaryModel.getGeoLocation() == null ? "null" : String.valueOf(summaryModel.getGeoLocation().isObfuscated());
                break;
            case POSTALCODE:
                key = summaryModel.getPostalCode() == null ? "null" : summaryModel.getPostalCode();
                break;
            case COUNTRY:
                key = summaryModel.getCountry() == null ? "null" : summaryModel.getCountry();
                break;
            case CITY:
                key = summaryModel.getCity() == null ? "null" : summaryModel.getCity();
                break;
            case ADDRESS1:
                key = summaryModel.getAddress1() == null ? "null" : summaryModel.getAddress1();
                break;
            case ADDRESS2:
                key = summaryModel.getAddress2() == null ? "null" : summaryModel.getAddress2();
                break;
            case PROPERTYNAME:
                key = summaryModel.getPropertyName() == null ? "null" : summaryModel.getPropertyName();
                break;
            case VRBOAVGRATING:
                key = Optional.ofNullable(summaryModel.getGuestRating()).map(GuestRating::getVrbo)
                        .map(GuestRating.Rating::getAvgRating).orElse("null");
                break;
            case PROPERTYTYPENAME:
                key = Optional.ofNullable(summaryModel.getPropertyType()).map(PropertyType::getName)
                        .orElse("null");
                break;
            case "Locales":
                key = Optional.ofNullable(summaryModel.getPropertyId()).map(PropertyId::getExpedia)
                        .orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> resultHandlers = null;
        switch (type) {
            case PROVINCE:
                resultHandlers = province(workbook);
                break;
            case GEOLOCATIONLATITUDE:
                resultHandlers = geoLatitude(workbook);;
                break;
            case GEOLOCATIONLONGITUDE:
                resultHandlers = geoLongitude(workbook);;
                break;
            case GEOLOCATIONOBFUSCATED:
                resultHandlers = geoObfuscated(workbook);;
                break;
            case POSTALCODE:
                resultHandlers = postalCode(workbook);
                break;
            case COUNTRY:
                resultHandlers = country(workbook);
                break;
            case CITY:
                resultHandlers = city(workbook);
                break;
            case ADDRESS1:
                resultHandlers = address1(workbook);
                break;
            case ADDRESS2:
                resultHandlers = address2(workbook);
                break;
            case PROPERTYNAME:
                resultHandlers = propertyName(workbook);
                break;
            case VRBOAVGRATING:
                resultHandlers = avgRating(workbook);
                break;
            case PROPERTYTYPENAME:
                resultHandlers = propertyType(workbook);
                break;
            case "Locales":
                resultHandlers = locales(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> geoObfuscated(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("GeoObfuscatedInEND", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("GeoObfuscatedInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> geoLongitude(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("GeoLongitudeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().getLongitude())),
                new CellWrapper<>("GeoLongitudeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGeoLocation().getLongitude()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("GeoLongitudeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().getLongitude())),
                new CellWrapper<>("GeoLongitudeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGeoLocation().getLongitude()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> geoLatitude(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("GeoLatitudeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().getLatitude())),
                new CellWrapper<>("GeoLatitudeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGeoLocation().getLatitude()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("GeoLatitudeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().getLatitude())),
                new CellWrapper<>("GeoLatitudeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGeoLocation().getLatitude()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("GeoObfuscatedInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGeoLocation().isObfuscated())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> postalCode(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("PostalCodeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPostalCode())),
                new CellWrapper<>("PostalCodeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("PostalCodeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPostalCode())),
                new CellWrapper<>("PostalCodeInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> country(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("CountryInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getCountry())),
                new CellWrapper<>("CountryInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ConvertedCountryInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(new Locale("", e).getDisplayCountry())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("CountryInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getCountry())),
                new CellWrapper<>("CountryInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ConvertedCountryInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(new Locale("", e).getDisplayCountry())),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> province(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("ProvinceInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getProvince())),
                new CellWrapper<>("ProvinceInEDN", (r, i, m, e, s) ->
                {
                    String[] cs = e.split(";;");
                    r.createCell(i).setCellValue(cs[1]);
                }),
                new CellWrapper<>("ConvertedProvinceInEDN", (r, i, m, e, s) ->
                {
                    String[] cs = e.split(";;");
                    r.createCell(i).setCellValue(getProvinceName(cs[0], cs[1]));
                }),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("ProvinceInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getProvince())),
                new CellWrapper<>("ProvinceInEDN", (r, i, m, e, s) ->
                {
                    String[] cs = e.split(";;");
                    r.createCell(i).setCellValue(cs[1]);
                }),
                new CellWrapper<>("ConvertedProvinceInEDN", (r, i, m, e, s) ->
                {
                    String[] cs = e.split(";;");
                    r.createCell(i).setCellValue(getProvinceName(cs[0], cs[1]));
                }),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private String getProvinceName(String country, String provinceCode) {
        String country3Code = countryNameMapping.get(country);
        String provinceName = provinceNameMapping.get(country3Code + "-" + provinceCode);
        return provinceName == null ? (StringUtils.isBlank(provinceCode) ? "null" : provinceCode) : provinceName;
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> city(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("CityInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getCity())),
                new CellWrapper<>("CityInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("CityInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getCity())),
                new CellWrapper<>("CityInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> address1(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("Address1InSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getAddress1())),
                new CellWrapper<>("Address1InEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("Address1InSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getAddress1())),
                new CellWrapper<>("Address1InEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> address2(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("Address2InSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getAddress2())),
                new CellWrapper<>("Address2InEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("Address2InSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getAddress2())),
                new CellWrapper<>("Address2InEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> propertyName(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("PropertyNameInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyName())),
                new CellWrapper<>("PropertyNameInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("PropertyNameInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyName())),
                new CellWrapper<>("PropertyNameInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> avgRating(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("AvgRatingInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGuestRating().getVrbo().getAvgRating())),
                new CellWrapper<>("AvgRatingInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGuestRating().getVrbo().getAvgRating()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("AvgRatingInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getGuestRating().getVrbo().getAvgRating())),
                new CellWrapper<>("AvgRatingInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("ErrorValue", (r, i, m, e, s) ->
                {
                    String errorValue = "null".equals(e) ? e : new BigDecimal(e).abs()
                            .subtract(new BigDecimal(m.getGuestRating().getVrbo().getAvgRating()).abs())
                            .abs().toString();
                    r.createCell(i).setCellValue(errorValue);
                }),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> propertyType(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("PropertyTypeIdFromLDS", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyType().getId())),
                new CellWrapper<>("PropertyTypeInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyType().getName())),
                new CellWrapper<>("PropertyTypeInEDM", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false"))))
        ), Lists.newArrayList(
                new CellWrapper<>("PropertyTypeIdFromLDS", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyType().getId())),
                new CellWrapper<>("PropertyNameInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyType().getName())),
                new CellWrapper<>("PropertyNameInEDN", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e)),
                new CellWrapper<>("InstantBook", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toBool(
                                instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")))),
                new CellWrapper<>("DataNumberInGroup", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(Convert.toInt(s))),
                new CellWrapper<>("DataPercentageInGroup", (r, i, m, e, s) -> {
                    BigDecimal b = new BigDecimal(Convert.toStr(s))
                            .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
                    r.createCell(i).setCellValue(b.doubleValue());
                }),
                new CellWrapper<>("SampleExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft()))
        ));
    }

    private Pair<List<CellWrapper<SummaryModel>>, List<CellWrapper<SummaryModel>>> locales(XSSFWorkbook workbook) {
        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", (r, i, m, e, s) -> {
                    XSSFCell cell = r.createCell(i);
                    StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                            m.getPropertyId().getExpedia(), workbook, cell);
                }),
                new CellWrapper<>("VrboPropertyIdInSDP", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(m.getPropertyId().getVrbo())),
                new CellWrapper<>("VrboPropertyIdInMapping", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(
                                vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft())),
                new CellWrapper<>("Locales", (r, i, m, e, s) ->
                        r.createCell(i).setCellValue(e))
        ), Lists.newArrayList(
        ));
    }

}

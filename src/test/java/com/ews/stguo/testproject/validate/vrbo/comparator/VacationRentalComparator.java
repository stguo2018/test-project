package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.Bathrooms;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.Bedrooms;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.HouseRules;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.PropertyManager;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.PropertySize;
import com.ews.stguo.testproject.validate.vrbo.model.vacationrental.VacationRentalModel;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHILDRENPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HOSTLANGUAGES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MANAGERNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MAXOCCUPANCY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MEASUREMENT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NUMBEROFBATHROOMS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NUMBEROFBEDROOMS;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PARTIESOREVENTSPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PETSPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.SMOKINGPERMITTED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.UNITS;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VacationRentalComparator extends VrboComparator<VacationRentalModel> {

    public VacationRentalComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping,
                                    Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    @Override
    public String getFileType() {
        return "VacationRental";
    }

    @Override
    public Class<VacationRentalModel> getClazz() {
        return VacationRentalModel.class;
    }

    @Override
    public String getId(VacationRentalModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(VacationRentalModel model, Optional<JSONObject> response) {
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });

        Integer area = unit.map(u -> u.optInt("area")).orElse(null);
        String areaUnit = unit.map(u -> u.optString("areaUnit")).orElse(null);
        Integer measurement = Optional.ofNullable(model.getPropertySize()).map(PropertySize::getMeasurement).orElse(null);
        String units = Optional.ofNullable(model.getPropertySize()).map(PropertySize::getUnits).orElse(null);
        Integer area2 = convertMeasurement(areaUnit, units, area);
        if (area == null || area == 0 || Objects.equals(area2, measurement)) {
            updateCounter(MEASUREMENT);
        }
        if (Objects.equals(areaUnit, units)) {
            updateCounter(UNITS);
        }

        Optional<JSONObject> unitRentalPolicy = unit.map(u -> u.optJSONObject("unitRentalPolicy"));
        Integer guests = unitRentalPolicy.map(urp -> urp.optJSONObject("maximumOccupancyHouseRule")).map(moh -> moh.optInt("guests")).orElse(null);
        Integer maxOccupancy = Optional.ofNullable(model.getMaxOccupancy()).orElse(null);
        if (guests == null || Objects.equals(guests, maxOccupancy)) {
            updateCounter(MAXOCCUPANCY);
        }

        Boolean petsAllowed = unitRentalPolicy.map(urp -> urp.optBoolean("petsAllowed")).orElse(null);
        Boolean fPetsAllowed = Optional.ofNullable(model.getHouseRules())
                .map(HouseRules::getPetRules)
                .map(HouseRules.PetRules::isPetsPermitted).orElse(null);
        if (Objects.equals(petsAllowed, fPetsAllowed)) {
            updateCounter(PETSPERMITTED);
        }

        Boolean childrenAllowed = unitRentalPolicy.map(urp -> urp.optString("childrenAllowed"))
                .map(s -> Objects.equals("ALLOWED", s)).orElse(null);
        Boolean fChildrenAllowed = Optional.ofNullable(model.getHouseRules())
                .map(HouseRules::getChildRules)
                .map(HouseRules.ChildRules::isChildrenPermitted).orElse(null);
        if (Objects.equals(childrenAllowed, fChildrenAllowed)) {
            updateCounter(CHILDRENPERMITTED);
        }

        Boolean smokingAllowed = unitRentalPolicy.map(urp -> urp.optBoolean("smokingAllowed")).orElse(null);
        Boolean fSmokingAllowed = Optional.ofNullable(model.getHouseRules())
                .map(HouseRules::getSmokingRules)
                .map(HouseRules.SmokingRules::isSmokingPermitted).orElse(null);
        if (Objects.equals(smokingAllowed, fSmokingAllowed)) {
            updateCounter(SMOKINGPERMITTED);
        } else {
            updateNoMatchData(SMOKINGPERMITTED, model, smokingAllowed == null ? "null" : String.valueOf(smokingAllowed));
        }

        Boolean eventsAllowed = unitRentalPolicy.map(urp -> urp.optBoolean("eventsAllowed")).orElse(null);
        Boolean fEventsAllowed = Optional.ofNullable(model.getHouseRules())
                .map(HouseRules::getPartyOrEventRules)
                .map(HouseRules.PartyOrEventRules::isPartiesOrEventsPermitted).orElse(null);
        if (Objects.equals(eventsAllowed, fEventsAllowed)) {
            updateCounter(PARTIESOREVENTSPERMITTED);
        }

        Integer numberOfBathrooms = unit.map(u -> u.optInt("numberOfBathrooms")).orElse(null);
        Integer numberOfBedrooms = unit.map(u -> u.optInt("numberOfBedrooms")).orElse(null);
        Integer fNumberOfBathrooms = Optional.ofNullable(model.getBathrooms()).map(Bathrooms::getNumberOfBathrooms).orElse(null);
        Integer fNumberOfBedrooms = Optional.ofNullable(model.getBedrooms()).map(Bedrooms::getNumberOfBedrooms).orElse(null);
        if (numberOfBathrooms == null || Objects.equals(numberOfBathrooms, fNumberOfBathrooms)) {
            updateCounter(NUMBEROFBATHROOMS);
        }
        if (numberOfBedrooms == null || Objects.equals(numberOfBedrooms, fNumberOfBedrooms)) {
            updateCounter(NUMBEROFBEDROOMS);
        }
        Optional<JSONObject> contact = response.map(r -> r.optJSONObject("contact"));
        List<String> languages = contact.map(c -> c.optJSONArray("featureValues"))
                .map(fvs -> {
                    List<String> langs = new ArrayList<>();
                    if (fvs.length() > 0) {
                        for (int i = 0; i < fvs.length(); i++) {
                            String lang = Optional.ofNullable(fvs.optJSONObject(i))
                                    .map(fv -> fv.optJSONObject("feature"))
                                    .map(fe -> fe.optJSONObject("localizedName"))
                                    .map(ln -> ln.optJSONArray("texts"))
                                    .map(ts -> {
                                        String l = null;
                                        if (ts.length() > 0) {
                                            for (int j = 0; j < ts.length(); j++) {
                                                Optional<JSONObject> o = Optional.ofNullable(ts.optJSONObject(j));
                                                if (o.isPresent() && Objects.equals("en", o.get().optString("locale"))) {
                                                    l = o.get().optString("content");
                                                    break;
                                                }
                                            }
                                        }
                                        return l;
                                    }).orElse(null);
                            if (StringUtils.isNotBlank(lang)) {
                                langs.add(lang.toLowerCase());
                            }
                        }
                    }
                    return langs;
                }).orElse(new ArrayList<>());
        List<String> languages2 = contact.map(c -> c.optJSONArray("featureValues"))
                .map(fvs -> {
                    List<String> langs = new ArrayList<>();
                    if (fvs.length() > 0) {
                        for (int i = 0; i < fvs.length(); i++) {
                            String lang = Optional.ofNullable(fvs.optJSONObject(i))
                                    .map(fv -> fv.optJSONObject("feature"))
                                    .map(fe -> fe.optJSONObject("localizedName"))
                                    .map(ln -> ln.optJSONArray("texts"))
                                    .map(ts -> {
                                        String l = null;
                                        if (ts.length() > 0) {
                                            for (int j = 0; j < ts.length(); j++) {
                                                Optional<JSONObject> o = Optional.ofNullable(ts.optJSONObject(j));
                                                if (o.isPresent() && Objects.equals("en", o.get().optString("locale"))) {
                                                    l = o.get().optString("content");
                                                    break;
                                                }
                                            }
                                        }
                                        return l;
                                    }).orElse(null);
                            if (StringUtils.isNotBlank(lang)) {
                                langs.add(lang);
                            }
                        }
                    }
                    return langs;
                }).orElse(new ArrayList<>());
        Set<String> hostLanguages = Optional.ofNullable(model.getHostLanguages())
                .orElse(new ArrayList<>())
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet());
        int lMatchingCount = 0;
        if (CollectionUtils.isNotEmpty(languages)) {
            for (String language : languages) {
                if (CollectionUtils.isNotEmpty(hostLanguages) && hostLanguages.contains(language)) {
                    lMatchingCount++;
                }
            }
        }
        if (CollectionUtils.isEmpty(model.getHostLanguages()) && CollectionUtils.isNotEmpty(languages2)) {
            updateNoMatchData(HOSTLANGUAGES, model, CollectionUtils.isNotEmpty(languages2) ? String.join(",", languages2) : "Empty!!!");
        }
        if (CollectionUtils.isEmpty(languages) && CollectionUtils.isEmpty(hostLanguages)) {
            updateCounter(HOSTLANGUAGES + "(100%)");
        } else {
            double lRate = CollectionUtils.isNotEmpty(languages) ? (double) lMatchingCount / languages.size() : 0;
            if (lRate != 1 && CollectionUtils.isNotEmpty(model.getHostLanguages()) && CollectionUtils.isNotEmpty(languages2)) {
                // updateNoMatchData(HOSTLANGUAGES, model, CollectionUtils.isNotEmpty(languages2) ? String.join(",", languages2) : "Empty!!!");
            }
            if (lRate == 0) {
                updateCounter(HOSTLANGUAGES + "(0%)");
            } else if (lRate <= 0.25) {
                updateCounter(HOSTLANGUAGES + "(1~25%)");
            } else if (lRate <= 0.5) {
                updateCounter(HOSTLANGUAGES + "(26~50%)");
            } else if (lRate <= 0.75) {
                updateCounter(HOSTLANGUAGES + "(51~75%)");
            } else if (lRate < 1) {
                updateCounter(HOSTLANGUAGES + "(76~99%)");
            } else {
                updateCounter(HOSTLANGUAGES + "(100%)");
                // updateNoMatchData(HOSTLANGUAGES, model, CollectionUtils.isNotEmpty(languages2) ? String.join(",", languages2) : "Empty!!!");
            }
        }

        String name = contact.map(c -> c.optJSONObject("name"))
                .map(n -> n.optJSONArray("texts"))
                .map(ts -> {
                    if (ts.length() > 0) {
                        for (int i = 0; i < ts.length(); i++) {
                            Optional<JSONObject> o = Optional.ofNullable(ts.optJSONObject(i));
                            if (o.isPresent() && Objects.equals(TrimUtils.trim(o.get().optString("locale")), "en")) {
                                return o.get().optString("content") != null ? TrimUtils.trim(o.get().optString("content")) : null;
                            }
                        }
                    }
                    return null;
                }).orElse(null);
        String managerName = Optional.ofNullable(model.getPropertyManager()).map(PropertyManager::getName).map(String::toLowerCase).orElse(null);
        if (StringUtils.isBlank(name) || StringUtils.equalsIgnoreCase(name, managerName)) {
            updateCounter(MANAGERNAME);
        } else {
//            updateNoMatchData(model, StringUtils.isNotBlank(name) ? name : "null");
        }
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(HOSTLANGUAGES, SMOKINGPERMITTED);
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<VacationRentalModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<VacationRentalModel>>, List<CellWrapper<VacationRentalModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, VacationRentalModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<VacationRentalModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<VacationRentalModel>> entry : noMatchData.entrySet()) {
            for (VacationRentalModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, VacationRentalModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, VacationRentalModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, VacationRentalModel> p = entry.getValue().get(0);
                row = sheet2.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, p.getRight(), p.getLeft(), entry.getValue().size());
                }
            }
        }

        try (FileOutputStream out = new FileOutputStream(getFilePath(type))) {
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(String type) {
        String filePath = "E:\\ews-29840\\VacationRental\\";
        switch (type) {
            case HOSTLANGUAGES:
                filePath += "HostLanguages.xlsx";
                break;
            case SMOKINGPERMITTED:
                filePath += "SmokingPermitted.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, VacationRentalModel model) {
        String key = null;
        switch (type) {
            case HOSTLANGUAGES:
                key = Optional.ofNullable(model.getHostLanguages()).map(hl -> String.join(",", hl)).orElse("Empty!!!");
                break;
            case SMOKINGPERMITTED:
                key = Optional.ofNullable(model.getHouseRules()).map(HouseRules::getSmokingRules)
                        .map(HouseRules.SmokingRules::isSmokingPermitted).map(String::valueOf).orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<VacationRentalModel>>, List<CellWrapper<VacationRentalModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<VacationRentalModel>>, List<CellWrapper<VacationRentalModel>>> resultHandlers = null;
        switch (type) {
            case HOSTLANGUAGES:
                resultHandlers = hostLanguages(workbook);
                break;
            case SMOKINGPERMITTED:
                resultHandlers = smokingPermitted(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<VacationRentalModel>>, List<CellWrapper<VacationRentalModel>>> hostLanguages(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> expediaId = (r, i, m, e, s) -> {
            r.createCell(i).setCellValue(m.getPropertyId().getExpedia());
        };
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboLink = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(VacationRentalModel::getHostLanguages)
                        .map(hl -> hl.stream().map(String::toLowerCase).collect(Collectors.joining(","))).orElse("Empty!!!"));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboLink", vrboLink),
                new CellWrapper<>("HostLanguagesFromLCS", dataInSDP),
                new CellWrapper<>("HostLanguagesFromEDN", dataInEDN)
//                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
//                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
//                new CellWrapper<>("HeroImageLink", dataInSDP1),
//                new CellWrapper<>("ThumbnailImageLink", dataInSDP2),
//                new CellWrapper<>("InstantBook", instantBook),
//                new CellWrapper<>("SDPData", dataInSDP3)
        ), Lists.newArrayList(
//                new CellWrapper<>("HeroImageLink", dataInSDP1),
//                new CellWrapper<>("ThumbnailImageLink", dataInSDP2),
//                new CellWrapper<>("InstantBook", instantBook),
//                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
//                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
//                new CellWrapper<>("SampleExpediaId", expediaId),
//                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
//                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<VacationRentalModel>>, List<CellWrapper<VacationRentalModel>>> smokingPermitted(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> expediaId = (r, i, m, e, s) -> {
            r.createCell(i).setCellValue(m.getPropertyId().getExpedia());
        };
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboLink = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("53885"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(VacationRentalModel::getHouseRules)
                        .map(HouseRules::getSmokingRules).map(HouseRules.SmokingRules::isSmokingPermitted)
                        .map(String::valueOf).orElse("null"));
        PentaConsumer<XSSFRow, Integer, VacationRentalModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboLink", vrboLink),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("SmokingPermittedInSDP", dataInSDP),
                new CellWrapper<>("SmokingPermittedInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("SmokingPermittedInSDP", dataInSDP),
                new CellWrapper<>("SmokingPermittedInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Integer convertMeasurement(String fromUnits, String toUnits, Integer measurement) {
        if (StringUtils.isBlank(fromUnits) || StringUtils.isBlank(toUnits) || measurement == null) {
            return measurement;
        }
        if (StringUtils.equalsIgnoreCase(toUnits, "SQUARE_FEET") && StringUtils.equalsIgnoreCase(fromUnits, "METERS_SQUARED")) {
            return new BigDecimal(String.valueOf(measurement))
                    .multiply(new BigDecimal("10.7639104"))
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        } else if (StringUtils.equalsIgnoreCase(toUnits, "SQUARE_METERS") && StringUtils.equalsIgnoreCase(fromUnits, "SQUARE_FEET")) {
            return new BigDecimal(String.valueOf(measurement))
                    .multiply(new BigDecimal("0.092903"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return measurement;
    }

}

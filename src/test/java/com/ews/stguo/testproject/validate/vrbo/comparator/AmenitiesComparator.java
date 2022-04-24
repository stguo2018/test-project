package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.amenities.AmenitiesModel;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CLEANLINESSANDSAFETY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYAMENITIES;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ROOMAMENITIES;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class AmenitiesComparator extends VrboComparator<AmenitiesModel> {

    public AmenitiesComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    private int count;

    @Override
    public String getFileType() {
        return "Amenities";
    }

    @Override
    public Class<AmenitiesModel> getClazz() {
        return AmenitiesModel.class;
    }

    @Override
    public String getId(AmenitiesModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(AmenitiesModel model, Optional<JSONObject> response) {
        if (count++ < 1000) {
            updateNoMatchData("Amenities", model, "null");
        }
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });

        Optional<JSONArray> featureValues = unit.map(u -> u.optJSONArray("featureValues"));
        Set<String> propertyAndRoomAmenities = featureValues.map(fvs -> {
            Set<String> as = new HashSet<>();
            for (int i = 0; i < fvs.length(); i++) {
                Optional<JSONObject> fv = Optional.ofNullable(fvs.optJSONObject(i));
                Optional<JSONObject> feature = fv.map(f -> f.optJSONObject("feature"));
                String amenityName = feature.map(f -> f.optString("name")).map(TrimUtils::trim).map(String::toLowerCase).orElse(null);
                if (StringUtils.isNotBlank(amenityName)) {
                    as.add(amenityName);
                }
            }
            return as;
        }).orElse(new HashSet<>());

        List<String> fPropertyAmenities = Optional.ofNullable(model.getPropertyAmenities())
                .map(Map::values)
                .map(Collection::stream)
                .map(s -> s.flatMap(Collection::stream))
                .map(s -> s.map(TrimUtils::trim))
                .map(s -> s.map(String::toLowerCase))
                .map(s -> s.collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        List<String> fRoomAmenities = Optional.ofNullable(model.getRoomAmenities())
                .map(Map::values)
                .map(Collection::stream)
                .map(s -> s.flatMap(Collection::stream))
                .map(s -> s.map(TrimUtils::trim))
                .map(s -> s.map(String::toLowerCase))
                .map(s -> s.collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        int pMatchingCount = 0;
        int rMatchingCount = 0;
        if (CollectionUtils.isNotEmpty(fPropertyAmenities)) {
            for (String fPropertyAmenity : fPropertyAmenities) {
                if (propertyAndRoomAmenities.contains(fPropertyAmenity)) {
                    pMatchingCount++;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(fRoomAmenities)) {
            for (String fRoomAmenity : fRoomAmenities) {
                if (propertyAndRoomAmenities.contains(fRoomAmenity)) {
                    rMatchingCount++;
                }
            }
        }
        double pRate = CollectionUtils.isNotEmpty(fPropertyAmenities) ? (double) pMatchingCount / fPropertyAmenities.size() : 0;
        double rRate = CollectionUtils.isNotEmpty(fRoomAmenities) ? (double) rMatchingCount / fRoomAmenities.size() : 0;
        if (CollectionUtils.isEmpty(propertyAndRoomAmenities) && CollectionUtils.isEmpty(fPropertyAmenities)) {
            updateCounter(PROPERTYAMENITIES + "(100%)");
        } else {
            if (pRate == 0) {
                updateCounter(PROPERTYAMENITIES + "(0%)");
            } else if (pRate <= 0.25) {
                updateCounter(PROPERTYAMENITIES + "(1~25%)");
            } else if (pRate <= 0.5) {
                updateCounter(PROPERTYAMENITIES + "(26~50%)");
            } else if (pRate <= 0.75) {
                updateCounter(PROPERTYAMENITIES + "(51~75%)");
            } else if (pRate < 1) {
                updateCounter(PROPERTYAMENITIES + "(76~99%)");
            } else {
                updateCounter(PROPERTYAMENITIES + "(100%)");
            }
        }
        if (CollectionUtils.isEmpty(propertyAndRoomAmenities) && CollectionUtils.isEmpty(fRoomAmenities)) {
            updateCounter(ROOMAMENITIES + "(100%)");
        } else {
            if (rRate == 0) {
                updateCounter(ROOMAMENITIES + "(0%)");
            } else if (rRate <= 0.25) {
                updateCounter(ROOMAMENITIES + "(1~25%)");
            } else if (rRate <= 0.5) {
                updateCounter(ROOMAMENITIES + "(26~50%)");
            } else if (rRate <= 0.75) {
                updateCounter(ROOMAMENITIES + "(51~75%)");
            } else if (rRate < 1) {
                updateCounter(ROOMAMENITIES + "(76~99%)");
            } else {
                updateCounter(ROOMAMENITIES + "(100%)");
            }
        }

        Set<String> cleanlinessAndSafety = response.map(re -> {
            JSONArray fvs = re.optJSONArray("featureValues");
            Set<String> cas = new HashSet<>();
            if (fvs != null && fvs.length() > 0) {
                for (int i = 0; i < fvs.length(); i++) {
                    Optional.of(fvs.optJSONObject(i)).map(fv -> fv.optJSONObject("feature"))
                            .map(fe -> fe.optString("name"))
                            .map(TrimUtils::trim)
                            .map(String::toLowerCase)
                            .ifPresent(cas::add);
                }
            }
            return cas;
        }).orElse(new HashSet<>());

        List<String> fCleanlinessAndSafety = Optional.ofNullable(model.getCleanlinessAndSafety())
                .map(Map::values)
                .map(Collection::stream)
                .map(s -> s.flatMap(Collection::stream))
                .map(s -> s.map(TrimUtils::trim))
                .map(s -> s.map(String::toLowerCase))
                .map(s -> s.collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        int cMatchingCount = 0;
        if (CollectionUtils.isNotEmpty(fCleanlinessAndSafety)) {
            for (String ca : fCleanlinessAndSafety) {
                if (cleanlinessAndSafety.contains(ca)) {
                    cMatchingCount++;
                }
            }
        }
        double cRate = CollectionUtils.isNotEmpty(fCleanlinessAndSafety) ? (double) cMatchingCount / fCleanlinessAndSafety.size() : 0;
        if (CollectionUtils.isEmpty(cleanlinessAndSafety) && CollectionUtils.isEmpty(fCleanlinessAndSafety)) {
            updateCounter(CLEANLINESSANDSAFETY + "(100%)");
        } else {
            if (cRate == 0) {
                updateCounter(CLEANLINESSANDSAFETY + "(0%)");
            } else if (cRate <= 0.25) {
                updateCounter(CLEANLINESSANDSAFETY + "(1~25%)");
            } else if (cRate <= 0.5) {
                updateCounter(CLEANLINESSANDSAFETY + "(26~50%)");
            } else if (cRate <= 0.75) {
                updateCounter(CLEANLINESSANDSAFETY + "(51~75%)");
            } else if (cRate < 1) {
                updateCounter(CLEANLINESSANDSAFETY + "(76~99%)");
            } else {
                updateCounter(CLEANLINESSANDSAFETY + "(100%)");
            }
        }
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList("Amenities");
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<AmenitiesModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<AmenitiesModel>>, List<CellWrapper<AmenitiesModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, AmenitiesModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<AmenitiesModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<AmenitiesModel>> entry : noMatchData.entrySet()) {
            for (AmenitiesModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, AmenitiesModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, AmenitiesModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, AmenitiesModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\Amenities\\";
        switch (type) {
            case "Amenities":
                filePath += "Amenities.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, AmenitiesModel model) {
        String key = null;
        switch (type) {
            case "Amenities":
                key = UUID.randomUUID().toString();
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<AmenitiesModel>>, List<CellWrapper<AmenitiesModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<AmenitiesModel>>, List<CellWrapper<AmenitiesModel>>> resultHandlers = null;
        switch (type) {
            case "Amenities":
                resultHandlers = image(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<AmenitiesModel>>, List<CellWrapper<AmenitiesModel>>> image(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> dataInSDP1 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(AmenitiesModel::getPropertyAmenities).map(JSONUtil::toJsonStr).orElse("null"));
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> dataInSDP2 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(AmenitiesModel::getRoomAmenities).map(JSONUtil::toJsonStr).orElse("null"));
        PentaConsumer<XSSFRow, Integer, AmenitiesModel, String, Object> dataInSDP3 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(AmenitiesModel::getCleanlinessAndSafety).map(JSONUtil::toJsonStr).orElse("null"));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("PropertyAmenities", dataInSDP1),
                new CellWrapper<>("RoomAmenities", dataInSDP2),
                new CellWrapper<>("CleanlinessAndSafety", dataInSDP3),
        new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("HeroImageLink", dataInSDP1),
                new CellWrapper<>("ThumbnailImageLink", dataInSDP2),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

}

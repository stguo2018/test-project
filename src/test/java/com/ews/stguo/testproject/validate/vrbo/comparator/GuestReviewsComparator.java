package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.guestreviews.GuestReviewsModel;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GuestRating;
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
import java.util.Optional;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOAVGRATING;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOREVIEWCOUNT;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class GuestReviewsComparator extends VrboComparator<GuestReviewsModel> {

    public GuestReviewsComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    @Override
    public String getFileType() {
        return "GuestReviews";
    }

    @Override
    public Class<GuestReviewsModel> getClazz() {
        return GuestReviewsModel.class;
    }

    @Override
    public String getId(GuestReviewsModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(GuestReviewsModel model, Optional<JSONObject> response) {
        updateCounter("activeCount");
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });

        Optional<JSONObject> reviewSummary = unit.map(u -> u.optJSONObject("reviewSummary"));
        BigDecimal avgRating = reviewSummary.map(rs -> rs.optDouble("averageRating")).map(String::valueOf).map(BigDecimal::new).orElse(null);
        String fAvgRating = Optional.ofNullable(model.getGuestRating()).map(GuestRating::getVrbo).map(GuestRating.Rating::getAvgRating).orElse(null);
        if (avgRating == null && fAvgRating == null) {
            updateCounter(VRBOAVGRATING);
        } else if (avgRating != null && fAvgRating != null && avgRating.abs().subtract(new BigDecimal(fAvgRating).abs()).abs()
                .compareTo(new BigDecimal("0.000001")) <= 0) {
            updateCounter(VRBOAVGRATING);
        } else {
            updateNoMatchData(VRBOAVGRATING, model, avgRating == null ? "null" : String.valueOf(avgRating));
        }
        Integer reviewCount = reviewSummary.map(rs -> rs.optInt("reviewCount")).orElse(null);
        String fReviewCount = Optional.ofNullable(model.getGuestRating()).map(GuestRating::getVrbo).map(GuestRating.Rating::getReviewCount).orElse(null);
        if (reviewCount == null) {
            updateCounter(VRBOREVIEWCOUNT);
        } else if (fReviewCount != null && reviewCount == Integer.parseInt(fReviewCount)) {
            updateCounter(VRBOREVIEWCOUNT);
        } else {
            updateNoMatchData(VRBOREVIEWCOUNT, model, reviewCount == null ? "null" : String.valueOf(reviewCount));
        }
    }

    @Override
    protected String getFileNameFormat() {
        return "ews-29840/" + getFileType() + "/expedia-lodging-guestreviews-%d-all.jsonl";
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(VRBOAVGRATING, VRBOREVIEWCOUNT);
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<GuestReviewsModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<GuestReviewsModel>>, List<CellWrapper<GuestReviewsModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, GuestReviewsModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<GuestReviewsModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<GuestReviewsModel>> entry : noMatchData.entrySet()) {
            for (GuestReviewsModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, GuestReviewsModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, GuestReviewsModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, GuestReviewsModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\GuestReviews\\";
        switch (type) {
            case VRBOAVGRATING:
                filePath += "GuestReviews-AvgRating.xlsx";
                break;
            case VRBOREVIEWCOUNT:
                filePath += "GuestReviews-ReviewCount.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, GuestReviewsModel model) {
        String key = null;
        switch (type) {
            case VRBOAVGRATING:
                key = Optional.ofNullable(model).map(GuestReviewsModel::getGuestRating)
                        .map(GuestRating::getVrbo).map(GuestRating.Rating::getAvgRating).orElse("null");
                break;
            case VRBOREVIEWCOUNT:
                key =Optional.ofNullable(model).map(GuestReviewsModel::getGuestRating)
                        .map(GuestRating::getVrbo).map(GuestRating.Rating::getReviewCount).orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<GuestReviewsModel>>, List<CellWrapper<GuestReviewsModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<GuestReviewsModel>>, List<CellWrapper<GuestReviewsModel>>> resultHandlers = null;
        switch (type) {
            case VRBOAVGRATING:
                resultHandlers = vrboAvgRating(workbook);
                break;
            case VRBOREVIEWCOUNT:
                resultHandlers = vrboAvgCount(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<GuestReviewsModel>>, List<CellWrapper<GuestReviewsModel>>> vrboAvgRating(XSSFWorkbook workbook) {
        // ExpediaId
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        // VrboPropertyId
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        // VrboPropertyIdInMapping
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        // InstantBook
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        // DataNumberInGroup
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        // DataPercentageInGroup
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("18031"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        // DataInSDP
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(GuestReviewsModel::getGuestRating)
                        .map(GuestRating::getVrbo).map(GuestRating.Rating::getAvgRating).orElse("null"));
        // DataInEDN
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> errorValue = (r, i, m, e, s) -> {
            String ev = "null".equals(e) ? e : new BigDecimal(e).abs()
                    .subtract(new BigDecimal(m.getGuestRating().getVrbo().getAvgRating()).abs())
                    .abs().toString();
            r.createCell(i).setCellValue(ev);
        };

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("AvgRatingInSDP", dataInSDP),
                new CellWrapper<>("AvgRatingInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("AvgRatingInSDP", dataInSDP),
                new CellWrapper<>("AvgRatingInEDN", dataInEDN),
                new CellWrapper<>("ErrorValue", errorValue),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<GuestReviewsModel>>, List<CellWrapper<GuestReviewsModel>>> vrboAvgCount(XSSFWorkbook workbook) {
        // ExpediaId
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        // VrboPropertyId
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        // VrboPropertyIdInMapping
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        // InstantBook
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        // DataNumberInGroup
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        // DataPercentageInGroup
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("18031"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        // DataInSDP
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(GuestReviewsModel::getGuestRating)
                        .map(GuestRating::getVrbo).map(GuestRating.Rating::getReviewCount).orElse("null"));
        // DataInEDN
        PentaConsumer<XSSFRow, Integer, GuestReviewsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("ReviewCountInSDP", dataInSDP),
                new CellWrapper<>("ReviewCountInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("ReviewCountInSDP", dataInSDP),
                new CellWrapper<>("ReviewCountInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

}

package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.listings.ListingsModel;
import com.ews.stguo.testproject.validate.vrbo.model.listings.VrboPropertyType;
import com.ews.stguo.testproject.validate.vrbo.model.summary.PropertyType;
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
import java.util.Optional;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.INSTANTBOOK;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.INVENTORYSOURCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPEID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYTYPENAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.QUOTEANDHOLD;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ListingsComparator extends VrboComparator<ListingsModel>  {

    public ListingsComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    @Override
    public String getFileType() {
        return "Listings";
    }

    @Override
    public Class<ListingsModel> getClazz() {
        return ListingsModel.class;
    }

    @Override
    public String getId(ListingsModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(ListingsModel model, Optional<JSONObject> response) {
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });

        String propertyType = unit.map(u -> u.optString("propertyType")).map(TrimUtils::trim).orElse(null);
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
        Boolean cibEnabled = unit.map(u -> u.optJSONObject("ecomUnitDerivedDataFlags")).map(eud -> eud.optBoolean("cibEnabled")).orElse(null);
        Boolean instantBook = unit.map(u -> u.optBoolean("instantBook")).orElse(null);
        Boolean fInstantBook = Optional.ofNullable(model.getVrboPropertyType()).map(VrboPropertyType::getInstantBook).orElse(null);
        if (fInstantBook != null && (instantBook.booleanValue() == fInstantBook.booleanValue() || (fInstantBook.booleanValue() == true && fInstantBook.booleanValue() == cibEnabled.booleanValue()) )) {
            updateCounter(INSTANTBOOK);
        } else {
            updateNoMatchData(INSTANTBOOK, model, String.valueOf(instantBook) + ":" + String.valueOf(cibEnabled));
        }
//        if (BooleanUtils.isTrue(fInstantBook) && BooleanUtils.isFalse(instantBook) && BooleanUtils.isTrue(cibEnabled)) {
//            updateNoMatchData(INSTANTBOOK, model, String.valueOf(instantBook) + ":" + String.valueOf(cibEnabled));
//        }
        Boolean fQuoteAndHold = Optional.ofNullable(model.getVrboPropertyType()).map(VrboPropertyType::getQuoteAndHold).orElse(null);
        if (fQuoteAndHold != null && fQuoteAndHold.booleanValue() != instantBook.booleanValue()) {
            updateCounter(QUOTEANDHOLD);
        } else {
            updateNoMatchData(QUOTEANDHOLD, model, String.valueOf(!instantBook));
        }
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(PROPERTYTYPENAME, INSTANTBOOK, QUOTEANDHOLD);
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<ListingsModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, ListingsModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<ListingsModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<ListingsModel>> entry : noMatchData.entrySet()) {
            for (ListingsModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, ListingsModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, ListingsModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, ListingsModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\Listings\\";
        switch (type) {
            case PROPERTYTYPENAME:
                filePath += "Listings-PropertyType.xlsx";
                break;
            case INSTANTBOOK:
                filePath += "Listings-InstantBook.xlsx";
                break;
            case QUOTEANDHOLD:
                filePath += "Listings-QuoteAndHold.xlsx";
                break;
            case INVENTORYSOURCE:
                filePath += "InventorySource.txt";
                break;
        }
        return filePath;
    }

    private String getKey(String type, ListingsModel model) {
        String key = null;
        switch (type) {
            case PROPERTYTYPENAME:
                key = Optional.ofNullable(model).map(ListingsModel::getPropertyType)
                        .map(PropertyType::getName).orElse("null");
                break;
            case INSTANTBOOK:
                key = Optional.ofNullable(model).map(ListingsModel::getVrboPropertyType)
                        .map(VrboPropertyType::getInstantBook).map(String::valueOf).orElse("null");
                break;
            case QUOTEANDHOLD:
                key = Optional.ofNullable(model).map(ListingsModel::getVrboPropertyType)
                        .map(VrboPropertyType::getQuoteAndHold).map(String::valueOf).orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> resultHandlers = null;
        switch (type) {
            case PROPERTYTYPENAME:
                resultHandlers = propertyType(workbook);
                break;
            case INSTANTBOOK:
                resultHandlers = instantBook(workbook);
                break;
            case QUOTEANDHOLD:
                resultHandlers = quoteAndHold(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> propertyType(XSSFWorkbook workbook) {
        // ExpediaId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        // VrboPropertyId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        // VrboPropertyIdInMapping
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        // InstantBook
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        // DataNumberInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        // DataPercentageInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        // DataInSDP
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ListingsModel::getPropertyType)
                        .map(PropertyType::getName).orElse("null"));
        // DataInEDN
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);
        // PropertyTypeIdInSDP
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> propertyTypeIdInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ListingsModel::getPropertyType)
                        .map(PropertyType::getId).map(String::valueOf).orElse("null"));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("PropertyTypeIdInSDP", propertyTypeIdInSDP),
                new CellWrapper<>("PropertyTypeNameInSDP", dataInSDP),
                new CellWrapper<>("PropertyTypeNameInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("PropertyTypeIdInSDP", propertyTypeIdInSDP),
                new CellWrapper<>("PropertyTypeNameInSDP", dataInSDP),
                new CellWrapper<>("PropertyTypeNameInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> instantBook(XSSFWorkbook workbook) {
        // ExpediaId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        // VrboPropertyId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        // VrboPropertyIdInMapping
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        // DataNumberInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        // DataPercentageInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        // DataInSDP
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ListingsModel::getVrboPropertyType)
                        .map(VrboPropertyType::getInstantBook).map(String::valueOf).orElse("null"));
        // DataInEDN
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e.split(":")[0]);
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> conditionInstantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e.split(":")[1]);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("InstantBookInSDP", dataInSDP),
                new CellWrapper<>("InstantBookInEDN", dataInEDN),
                new CellWrapper<>("ConditionInstantBook", conditionInstantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("InstantBookInSDP", dataInSDP),
                new CellWrapper<>("InstantBookInEDN", dataInEDN),
                new CellWrapper<>("ConditionInstantBook", conditionInstantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<ListingsModel>>, List<CellWrapper<ListingsModel>>> quoteAndHold(XSSFWorkbook workbook) {
        // ExpediaId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        // VrboPropertyId
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        // VrboPropertyIdInMapping
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        // DataNumberInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        // DataPercentageInGroup
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        // DataInSDP
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ListingsModel::getVrboPropertyType)
                        .map(VrboPropertyType::getQuoteAndHold).map(String::valueOf).orElse("null"));
        // DataInEDN
        PentaConsumer<XSSFRow, Integer, ListingsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("QuoteAndHoldInSDP", dataInSDP),
                new CellWrapper<>("QuoteAndHoldInEDN", dataInEDN)
        ), Lists.newArrayList(
                new CellWrapper<>("QuoteAndHoldInSDP", dataInSDP),
                new CellWrapper<>("QuoteAndHoldInEDN", dataInEDN),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

}

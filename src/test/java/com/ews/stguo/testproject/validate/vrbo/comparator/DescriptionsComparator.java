package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.descriptions.DescriptionsModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Lists;
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

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYDESCRIPTION;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class DescriptionsComparator extends VrboComparator<DescriptionsModel> {

    public DescriptionsComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    @Override
    public String getFileType() {
        return "Descriptions";
    }

    @Override
    public Class<DescriptionsModel> getClazz() {
        return DescriptionsModel.class;
    }

    @Override
    public String getId(DescriptionsModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(DescriptionsModel model, Optional<JSONObject> response) {
        Optional<JSONObject> adContent = response.map(rs -> rs.optJSONObject("adContent"));
        Optional<JSONObject> description = adContent.map(ac -> ac.optJSONObject("description"));
        String propertyDescription = description.map(ds -> ds.optJSONArray("texts")).map(ts -> {
            if (ts.length() > 0) {
                for (int i = 0; i < ts.length(); i++) {
                    Optional<JSONObject> text = Optional.ofNullable(ts.optJSONObject(i));
                    String locale = text.map(t -> t.optString("locale")).orElse(null);
                    if ("en".equalsIgnoreCase(locale)) {
                        return text.map(t -> t.optString("content")).orElse(null);
                    }
                }
            }
            return null;
        }).orElse(null);
        if (Objects.equals(propertyDescription, model.getPropertyDescription())) {
            updateCounter(PROPERTYDESCRIPTION);
        }
        updateNoMatchData(PROPERTYDESCRIPTION, model, StringUtils.isBlank(propertyDescription) ? "null" : propertyDescription);
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(PROPERTYDESCRIPTION);
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<DescriptionsModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<DescriptionsModel>>, List<CellWrapper<DescriptionsModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, DescriptionsModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<DescriptionsModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<DescriptionsModel>> entry : noMatchData.entrySet()) {
            for (DescriptionsModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, DescriptionsModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, DescriptionsModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, DescriptionsModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\Descriptions\\";
        switch (type) {
            case PROPERTYDESCRIPTION:
                filePath += "Descriptions-PropertyDescription.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, DescriptionsModel model) {
        String key = null;
        switch (type) {
            case PROPERTYDESCRIPTION:
                key = Optional.ofNullable(model).map(DescriptionsModel::getPropertyDescription).orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<DescriptionsModel>>, List<CellWrapper<DescriptionsModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<DescriptionsModel>>, List<CellWrapper<DescriptionsModel>>> resultHandlers = null;
        switch (type) {
            case PROPERTYDESCRIPTION:
                resultHandlers = propertyDescriptions(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<DescriptionsModel>>, List<CellWrapper<DescriptionsModel>>> propertyDescriptions(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(DescriptionsModel::getPropertyDescription).orElse("null"));
        PentaConsumer<XSSFRow, Integer, DescriptionsModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("PropertyDescriptionInSDP", dataInSDP),
                new CellWrapper<>("PropertyDescriptionInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("PropertyDescriptionInSDP", dataInSDP),
                new CellWrapper<>("PropertyDescriptionInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }
}

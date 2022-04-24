package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.images.Hero;
import com.ews.stguo.testproject.validate.vrbo.model.images.ImagesModel;
import com.ews.stguo.testproject.validate.vrbo.model.images.Thumbnail;
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
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ImagesComparator extends VrboComparator<ImagesModel> {

    private int count = 0;
    
    public ImagesComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }
    
    @Override
    public String getFileType() {
        return "Images";
    }

    @Override
    public Class<ImagesModel> getClazz() {
        return ImagesModel.class;
    }

    @Override
    public String getId(ImagesModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(ImagesModel model, Optional<JSONObject> response) {
        if (count++ < 1000) {
            updateNoMatchData("image", model, "null");
        }
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList("image");
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<ImagesModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<ImagesModel>>, List<CellWrapper<ImagesModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, ImagesModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<ImagesModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<ImagesModel>> entry : noMatchData.entrySet()) {
            for (ImagesModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, ImagesModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, ImagesModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, ImagesModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\Images\\";
        switch (type) {
            case "image":
                filePath += "Images.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, ImagesModel model) {
        String key = null;
        switch (type) {
            case "image":
                key = Optional.ofNullable(model.getHero()).map(Hero::getLink).orElse(null);
                key = key == null ? Optional.ofNullable(model.getThumbnail()).map(Thumbnail::getLink).orElse("null") : key;
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<ImagesModel>>, List<CellWrapper<ImagesModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<ImagesModel>>, List<CellWrapper<ImagesModel>>> resultHandlers = null;
        switch (type) {
            case "image":
                resultHandlers = image(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<ImagesModel>>, List<CellWrapper<ImagesModel>>> image(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> dataInSDP1 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ImagesModel::getHero).map(Hero::getLink).orElse("null"));
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> dataInSDP2 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(ImagesModel::getThumbnail).map(Thumbnail::getLink).orElse("null"));
        PentaConsumer<XSSFRow, Integer, ImagesModel, String, Object> dataInSDP3 = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(JSONUtil.toJsonStr(m));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("HeroImageLink", dataInSDP1),
                new CellWrapper<>("ThumbnailImageLink", dataInSDP2),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("SDPData", dataInSDP3)
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

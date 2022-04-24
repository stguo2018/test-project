package com.ews.stguo.testproject.validate.vrbo.comparator;

import cn.hutool.core.convert.Convert;
import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.utils.poi.StyleCommons;
import com.ews.stguo.testproject.utils.poi.WriteCommons;
import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import com.ews.stguo.testproject.validate.vrbo.model.policies.PaymentPolicy;
import com.ews.stguo.testproject.validate.vrbo.model.policies.PoliciesModel;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKINSTARTTIME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHECKOUTTIME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CHILDRENANDEXTRABEDPOLICY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.MINIMUMAGE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PAYMENTPOLICYLOCALCURRENCY;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class PoliciesComparator extends VrboComparator<PoliciesModel> {

    public PoliciesComparator(Set<Integer> hotelIds, Map<String, Pair<String, String>> vrboWebLikeMapping, Map<String, String> instantBookMapping) {
        super(hotelIds, vrboWebLikeMapping, instantBookMapping);
    }

    @Override
    public String getFileType() {
        return "Policies";
    }

    private int ccc = 0;

    @Override
    public Class<PoliciesModel> getClazz() {
        return PoliciesModel.class;
    }

    @Override
    public String getId(PoliciesModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(PoliciesModel model, Optional<JSONObject> response) {
        Optional<JSONObject> unit = response.map(re -> {
            JSONArray units = re.optJSONArray("units");
            if (units != null && units.length() > 0) {
                return units.optJSONObject(0);
            }
            return null;
        });
        Optional<JSONObject> unitRentalPolicy = unit.map(u -> u.optJSONObject("unitRentalPolicy"));
        String checkInTime = unitRentalPolicy.map(urp -> urp.optString("checkInTime")).map(TrimUtils::trim).map(this::convertTime).orElse(null);
        String checkOutTime = unitRentalPolicy.map(urp -> urp.optString("checkOutTime")).map(TrimUtils::trim).map(this::convertTime).orElse(null);
        if (Objects.equals(checkInTime, convertTime(model.getCheckInStartTime()))) {
            updateCounter(CHECKINSTARTTIME);
        } else {
            updateNoMatchData(CHECKINSTARTTIME, model, checkInTime == null ? "null" : checkInTime);
        }
        if (Objects.equals(checkOutTime, convertTime(model.getCheckOutTime()))) {
            updateCounter(CHECKOUTTIME);
        } else {
            updateNoMatchData(CHECKOUTTIME, model, checkOutTime == null ? "null" : checkOutTime);
        }

        Integer minimumAge = unitRentalPolicy.map(urp -> urp.optJSONObject("minimumAgeHouseRule")).map(mhr -> mhr.optInt("age")).orElse(null);
        String minAge = model.getMinimumAge();
        if (minimumAge == null && minAge == null) {
            updateCounter(MINIMUMAGE);
        } else if (minimumAge != null && Objects.equals(String.valueOf(minimumAge), minAge)) {
            updateCounter(MINIMUMAGE);
        } else {
            updateNoMatchData(MINIMUMAGE, model, minimumAge == null ? "null" : String.valueOf(minimumAge));
        }

        String bookingCurrency = unit.map(u -> u.optString("bookingCurrency")).orElse(null);
        String localCurrency = Optional.ofNullable(model.getPaymentPolicy()).map(PaymentPolicy::getLocalCurrency).orElse(null);
        if (Objects.equals(bookingCurrency, localCurrency)) {
            updateCounter(PAYMENTPOLICYLOCALCURRENCY);
        } else {
            updateNoMatchData(PAYMENTPOLICYLOCALCURRENCY, model, StringUtils.isBlank(bookingCurrency) ? "null" : bookingCurrency);
        }

        if (ccc < 1000 && CollectionUtils.isNotEmpty(model.getChildrenAndExtraBedPolicy())) {
            updateNoMatchData(CHILDRENANDEXTRABEDPOLICY, model, "");
            ccc++;
        }
    }

    private String convertTime(String time) {
        if (StringUtils.isBlank(time)) {
            return time;
        }
        try {
            if (time.startsWith("12")) {
                if (time.contains("AM") || time.contains("am")) {
                    String[] columns = time.split(" ");
                    String[] columns2 = columns[0].split(":");
                    return "00:" + columns2[1];
                }
                if (time.contains("PM") || time.contains("pm")) {
                    String[] columns = time.split(" ");
                    String[] columns2 = columns[0].split(":");
                    return "12:" + columns2[1];
                }
            } else {
                if (time.contains("AM") || time.contains("am")) {
                    String t = time.split(" ")[0];
                    return t.length() == 4 ? "0" + t : t;
                }
                if (time.contains("PM") || time.contains("pm")) {
                    String[] columns = time.split(" ");
                    String[] columns2 = columns[0].split(":");
                    int hour = Integer.parseInt(columns2[0]) + 12;
                    String suffix = columns2.length > 1 ? columns2[1] : "00";
                    return hour + ":" + suffix;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    @Override
    public void recordNoMatchAsExcel() {
        List<String> types = Lists.newArrayList(CHECKINSTARTTIME, CHECKOUTTIME, MINIMUMAGE, PAYMENTPOLICYLOCALCURRENCY, CHILDRENANDEXTRABEDPOLICY);
        types.forEach(this::allWrite);
    }

    private void allWrite(String type) {
        Map<String, List<PoliciesModel>> noMatchData = getNoMatchData(type);
        int allSize = noMatchData.values().stream().mapToInt(List::size).sum();
        int rowIndex = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> resultHandlers = getResultHandlers(type, workbook);
        XSSFSheet sheet2 = workbook.createSheet("MismatchedGroup");
        Map<String, List<Pair<String, PoliciesModel>>> specialGroups = new HashMap<>();
        XSSFSheet sheet = workbook.createSheet("Full");

        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Full(" + allSize + ")");
        List<CellWrapper<PoliciesModel>> wrappers = resultHandlers.getLeft();
        row = sheet.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<PoliciesModel>> entry : noMatchData.entrySet()) {
            for (PoliciesModel model : entry.getValue()) {
                row = sheet.createRow(rowIndex++);
                for (int i = 0; i < wrappers.size(); i++) {
                    wrappers.get(i).getConsumer().accept(row, i, model, entry.getKey(), null);
                }
                String key = instantBookMapping.getOrDefault(model.getPropertyId().getExpedia(), "false")
                        + getKey(type, model) + entry.getKey();
                List<Pair<String, PoliciesModel>> pairs = specialGroups.computeIfAbsent(key, k -> new ArrayList<>());
                pairs.add(Pair.of(entry.getKey(), model));
            }
        }

        rowIndex = 0;
        row = sheet2.createRow(rowIndex++);
        row.createCell(0).setCellValue("NumberOfMismatchedGroup(" + specialGroups.size() + ")");
        wrappers = resultHandlers.getRight();
        row = sheet2.createRow(rowIndex++);
        WriteCommons.writeHeaders(row, wrappers);
        for (Map.Entry<String, List<Pair<String, PoliciesModel>>> entry : specialGroups.entrySet()) {
            if (Optional.ofNullable(entry).map(Map.Entry::getValue).map(vs -> vs.get(0)).isPresent()) {
                Pair<String, PoliciesModel> p = entry.getValue().get(0);
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
        String filePath = "E:\\ews-29840\\Policies\\";
        switch (type) {
            case CHECKINSTARTTIME:
                filePath += "Policies-CheckInStartTime.xlsx";
                break;
            case CHECKOUTTIME:
                filePath += "Policies-CheckoutTime.xlsx";
                break;
            case MINIMUMAGE:
                filePath += "Policies-MinimumAge.xlsx";
                break;
            case PAYMENTPOLICYLOCALCURRENCY:
                filePath += "Policies-PaymentPolicyLocalCurrency.xlsx";
                break;
            case CHILDRENANDEXTRABEDPOLICY:
                filePath += "Policies-ChildrenAndExtrabedPolicy.xlsx";
                break;
        }
        return filePath;
    }

    private String getKey(String type, PoliciesModel model) {
        String key = null;
        switch (type) {
            case CHECKINSTARTTIME:
                key = Optional.ofNullable(model).map(PoliciesModel::getCheckInStartTime).map(this::convertTime).orElse("null");
                break;
            case CHECKOUTTIME:
                key = Optional.ofNullable(model).map(PoliciesModel::getCheckOutTime).map(this::convertTime).orElse("null");
                break;
            case MINIMUMAGE:
                key = Optional.ofNullable(model).map(PoliciesModel::getMinimumAge).orElse("null");
                break;
            case PAYMENTPOLICYLOCALCURRENCY:
                key = Optional.ofNullable(model).map(PoliciesModel::getPaymentPolicy).map(PaymentPolicy::getLocalCurrency).orElse("null");
                break;
            case CHILDRENANDEXTRABEDPOLICY:
                key = Optional.ofNullable(model).map(PoliciesModel::getChildrenAndExtraBedPolicy).map(List::toString).orElse("null");
                break;
        }
        return key;
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> getResultHandlers(String type, XSSFWorkbook workbook) {
        Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> resultHandlers = null;
        switch (type) {
            case CHECKINSTARTTIME:
                resultHandlers = checkInStartTime(workbook);
                break;
            case CHECKOUTTIME:
                resultHandlers = checkoutTime(workbook);
                break;
            case MINIMUMAGE:
                resultHandlers = minimumAge(workbook);
                break;
            case PAYMENTPOLICYLOCALCURRENCY:
                resultHandlers = paymentPolicyLocalCurrency(workbook);
                break;
            case CHILDRENANDEXTRABEDPOLICY:
                resultHandlers = childrenAndExtrabedPolicy(workbook);
                break;
        }
        return resultHandlers;
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> checkInStartTime(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getCheckInStartTime).orElse("null"));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> convertedDataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getCheckInStartTime).map(this::convertTime).orElse("null"));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("CheckInStartTimeInSDP", dataInSDP),
                new CellWrapper<>("ConvertedCheckInStartTimeInSDP", convertedDataInSDP),
                new CellWrapper<>("CheckInStartTimeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("CheckInStartTimeInSDP", dataInSDP),
                new CellWrapper<>("ConvertedCheckInStartTimeInSDP", convertedDataInSDP),
                new CellWrapper<>("CheckInStartTimeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> checkoutTime(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getCheckOutTime).orElse("null"));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> convertedDataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getCheckOutTime).map(this::convertTime).orElse("null"));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("CheckoutTimeInSDP", dataInSDP),
                new CellWrapper<>("ConvertedCheckoutTimeInSDP", convertedDataInSDP),
                new CellWrapper<>("CheckoutTimeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("CheckoutTimeInSDP", dataInSDP),
                new CellWrapper<>("ConvertedCheckoutTimeInSDP", convertedDataInSDP),
                new CellWrapper<>("CheckoutTimeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> minimumAge(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getMinimumAge).orElse("null"));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("MinimumAgeInSDP", dataInSDP),
                new CellWrapper<>("MinimumAgeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("MinimumAgeInSDP", dataInSDP),
                new CellWrapper<>("MinimumAgeInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> paymentPolicyLocalCurrency(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getPaymentPolicy).map(PaymentPolicy::getLocalCurrency).orElse("null"));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInEDN = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(e);

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("LocalCurrencyInSDP", dataInSDP),
                new CellWrapper<>("LocalCurrencyInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("LocalCurrencyInSDP", dataInSDP),
                new CellWrapper<>("LocalCurrencyInEDN", dataInEDN),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

    private Pair<List<CellWrapper<PoliciesModel>>, List<CellWrapper<PoliciesModel>>> childrenAndExtrabedPolicy(XSSFWorkbook workbook) {
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> expediaId = (r, i, m, e, s) -> {
            XSSFCell cell = r.createCell(i);
            StyleCommons.setLinkToCell(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getRight(),
                    m.getPropertyId().getExpedia(), workbook, cell);
        };
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyId = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(m.getPropertyId().getVrbo());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> vrboPropertyIdInMapping = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(vrboWebLikeMapping.get(m.getPropertyId().getExpedia()).getLeft());
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> instantBook = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toBool(
                        instantBookMapping.getOrDefault(m.getPropertyId().getExpedia(), "false")));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataNumberInGroup = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Convert.toInt(s));
        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataPercentageInGroup = (r, i, m, e, s) -> {
            BigDecimal b = new BigDecimal(Convert.toStr(s))
                    .divide(new BigDecimal("51966"), 6, RoundingMode.HALF_UP);
            StyleCommons.setPercentageToCell(b.toString(), StyleCommons.FORMAT_PERCENT_FIVE, workbook, r.createCell(i));
        };

        PentaConsumer<XSSFRow, Integer, PoliciesModel, String, Object> dataInSDP = (r, i, m, e, s) ->
                r.createCell(i).setCellValue(Optional.ofNullable(m).map(PoliciesModel::getChildrenAndExtraBedPolicy)
                        .map(c -> c.stream().collect(Collectors.joining(","))).orElse("null"));

        return Pair.of(Lists.newArrayList(
                new CellWrapper<>("ExpediaId", expediaId),
                new CellWrapper<>("VrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("VrboPropertyIdInMapping", vrboPropertyIdInMapping),
                new CellWrapper<>("LocalCurrencyInSDP", dataInSDP),
                new CellWrapper<>("InstantBook", instantBook)
        ), Lists.newArrayList(
                new CellWrapper<>("LocalCurrencyInSDP", dataInSDP),
                new CellWrapper<>("InstantBook", instantBook),
                new CellWrapper<>("DataNumberInGroup", dataNumberInGroup),
                new CellWrapper<>("DataPercentageInGroup", dataPercentageInGroup),
                new CellWrapper<>("SampleExpediaId", expediaId),
                new CellWrapper<>("SampleVrboPropertyIdInSDP", vrboPropertyId),
                new CellWrapper<>("SampleVrboPropertyIdInMapping", vrboPropertyIdInMapping)
        ));
    }

}

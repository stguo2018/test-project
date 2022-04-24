package com.ews.stguo.testproject.utils.poi;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class StyleCommons {

    public static final String FORMAT_PERCENT_FIVE = "0.00000%";

    private static final Map<XSSFWorkbook, Map<String, XSSFCellStyle>> map = new HashMap<>();
    private static final Map<XSSFWorkbook, XSSFDataFormat> map2 = new HashMap<>();

    private static final String STYLE_LINK = "link";
    private static final String STYLE_PERCENTAGE = "percentage";

    private StyleCommons() {

    }

    public static void setLinkToCell(String link, String title, XSSFWorkbook workbook, XSSFCell cell) {
        cell.setCellFormula("HYPERLINK(\"" + link + "\",\"" + title + "\")");
        XSSFCellStyle linkStyle = getLinkStyle(workbook);
        cell.setCellStyle(linkStyle);
    }

    public static void setPercentageToCell(String value, String format, XSSFWorkbook workbook, XSSFCell cell) {
        cell.setCellValue(Double.parseDouble(value));
        XSSFCellStyle percentageStyle = getPercentageStyle(workbook);
        XSSFDataFormat dataFormat = getDataFormat(workbook);
        percentageStyle.setDataFormat(dataFormat.getFormat(format));
        cell.setCellStyle(percentageStyle);
    }

    private static XSSFCellStyle getLinkStyle(XSSFWorkbook workbook) {
        synchronized (workbook) {
            return map.computeIfAbsent(workbook, k -> new HashMap<>())
                    .computeIfAbsent(STYLE_LINK, k -> {
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont cellFont = workbook.createFont();
                        cellFont.setUnderline((byte) 1);
                        cellFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
                        cellStyle.setFont(cellFont);
                        return cellStyle;
                    });
        }
    }

    private static synchronized XSSFCellStyle getPercentageStyle(XSSFWorkbook workbook) {
        synchronized (workbook) {
            return map.computeIfAbsent(workbook, k -> new HashMap<>())
                    .computeIfAbsent(STYLE_PERCENTAGE, k -> workbook.createCellStyle());
        }
    }

    private static synchronized XSSFDataFormat getDataFormat(XSSFWorkbook workbook) {
        return map2.computeIfAbsent(workbook, k -> workbook.createDataFormat());
    }

}

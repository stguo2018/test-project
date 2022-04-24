package com.ews.stguo.testproject.utils.poi;

import com.ews.stguo.testproject.utils.poi.model.CellWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;

import java.util.List;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class WriteCommons {

    private WriteCommons() {

    }

    public static <T> void writeHeaders(XSSFRow row, List<CellWrapper<T>> cellWrappers) {
        if (CollectionUtils.isEmpty(cellWrappers)) {
            System.out.println("CellWrappers is null or empty.");
        }
        for (int i = 0; i < cellWrappers.size(); i++) {
            row.createCell(i).setCellValue(cellWrappers.get(i).getHeaderName());
        }
    }

}

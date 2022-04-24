package com.ews.stguo.testproject.utils.poi.model;

import com.ews.stguo.testproject.utils.poi.functions.PentaConsumer;
import org.apache.poi.xssf.usermodel.XSSFRow;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class CellWrapper<T> {

    private String headerName;
    private PentaConsumer<XSSFRow, Integer, T, String, Object> consumer;

    public CellWrapper() {

    }

    public CellWrapper(String headerName, PentaConsumer<XSSFRow, Integer, T, String, Object> consumer) {
        this.headerName = headerName;
        this.consumer = consumer;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public PentaConsumer<XSSFRow, Integer, T, String, Object> getConsumer() {
        return consumer;
    }

    public void setConsumer(PentaConsumer<XSSFRow, Integer, T, String, Object> consumer) {
        this.consumer = consumer;
    }
}

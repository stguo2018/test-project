package com.ews.stguo.testproject.validate.vrbo.verify;

import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SummaryVerify extends SDPVerify {

    @Test
    public void test01() throws Exception {
        verify("Summary", "sddp-feed-control-file (2).csv");
    }

    protected void dataStorage(Connection conn, JSONObject data, String ecomId, int fileIndex) throws Exception {
        String queryTemp = "insert into %s(%s) values(%s)";
        StringBuilder columns = new StringBuilder();
        List<Object> params = new ArrayList<>();
        columns.append("ecom_id");
        params.add(ecomId);
        Optional.ofNullable(data.optJSONObject("bookable")).ifPresent(bookable -> {
            columns.append(",expedia_bookable");
            params.add(bookable.optBoolean("expedia"));
            columns.append(",hcom_bookable");
            params.add(bookable.optBoolean("hcom"));
            columns.append(",vrbo_bookable");
            params.add(bookable.optBoolean("vrbo"));
        });
        String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
        String query1 = String.format(queryTemp, "bookable", columns, values);
        try (PreparedStatement preparedStatement = conn.prepareStatement(query1)) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                int index = i + 1;
                if (value instanceof Boolean) {
                    preparedStatement.setBoolean(index, BooleanUtils.toBoolean(String.valueOf(value)));
                } else {
                    preparedStatement.setString(index, String.valueOf(value));
                }
            }
            preparedStatement.executeUpdate();
        }
    }


}

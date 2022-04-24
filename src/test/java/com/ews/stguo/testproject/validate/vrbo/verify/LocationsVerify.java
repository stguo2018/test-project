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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocationsVerify extends SDPVerify {

    @Test
    public void test01() throws Exception {
        verify("Locations", "sddp-feed-control-file (2).csv");
    }

    protected void dataStorage(Connection conn, JSONObject data, String ecomId, int fileIndex) throws Exception {
        String queryTemp = "insert into %s(%s) values(%s)";
        StringBuilder columns = new StringBuilder();
        List<Object> params = new ArrayList<>();
        columns.append("ecom_id");
        params.add(ecomId);
        Optional.ofNullable(data.optString("address1")).ifPresent(v -> {
            columns.append(",address1");
            params.add(v);
        });
        Optional.ofNullable(data.optString("address2")).ifPresent(v -> {
            columns.append(",address2");
            params.add(v);
        });
        Optional.ofNullable(data.optString("city")).ifPresent(v -> {
            columns.append(",city");
            params.add(v);
        });
        Optional.ofNullable(data.optString("province")).ifPresent(v -> {
            columns.append(",province");
            params.add(v);
        });
        Optional.ofNullable(data.optString("country")).ifPresent(v -> {
            columns.append(",country");
            params.add(v);
        });
        Optional.ofNullable(data.optString("postalCode")).ifPresent(v -> {
            columns.append(",postal_code");
            params.add(v);
        });
        Optional.ofNullable(data.optJSONObject("geoLocation")).ifPresent(geoLocation -> {
            Optional.ofNullable(geoLocation.optString("latitude")).ifPresent(v -> {
                columns.append(",latitude");
                params.add(v);
            });
            Optional.ofNullable(geoLocation.optString("longitude")).ifPresent(v -> {
                columns.append(",longitude");
                params.add(v);
            });
            Optional.ofNullable(geoLocation.optString("obfuscated")).ifPresent(v -> {
                columns.append(",obfuscated");
                params.add(BooleanUtils.toBoolean(v));
            });
        });
        columns.append(",file_index");
        params.add(String.valueOf(fileIndex));
        String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
        String query1 = String.format(queryTemp, "location", columns, values);

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

        JSONArray phone = data.optJSONArray("phone");
        if (phone != null) {
            for (int i = 0; i < phone.length(); i++) {
                StringBuilder columns2 = new StringBuilder();
                List<String> params2 = new ArrayList<>();
                JSONObject p = phone.optJSONObject(i);
                columns2.append("phone_id");
                params2.add(UUID.randomUUID().toString());
                columns2.append(",ecom_id");
                params2.add(ecomId);
                Optional.ofNullable(p.optString("type")).ifPresent(v -> {
                    columns2.append(",type");
                    params2.add(v);
                });
                Optional.ofNullable(p.optString("countryCode")).ifPresent(v -> {
                    columns2.append(",country_code");
                    params2.add(v);
                });
                Optional.ofNullable(p.optString("areaCode")).ifPresent(v -> {
                    columns2.append(",area_code");
                    params2.add(v);
                });
                Optional.ofNullable(p.optString("number")).ifPresent(v -> {
                    columns2.append(",number");
                    params2.add(v);
                });
                Optional.ofNullable(p.optString("extension")).ifPresent(v -> {
                    columns2.append(",extension");
                    params2.add(v);
                });
                String values2 = params2.stream().map(a -> "?").collect(Collectors.joining(","));
                String query2 = String.format(queryTemp, "phone", columns2, values2);
                try (PreparedStatement preparedStatement = conn.prepareStatement(query2)) {
                    for (int j = 0; j < params2.size(); j++) {
                        String value = params2.get(j);
                        int index = j + 1;
                        preparedStatement.setString(index, String.valueOf(value));
                    }
                    preparedStatement.executeUpdate();
                }
            }
        }

        StringBuilder columns3 = new StringBuilder();
        List<String> params3 = new ArrayList<>();
        Optional.ofNullable(data.optJSONObject("locationAttribute")).ifPresent(la -> {
            columns3.append("ecom_id");
            params3.add(ecomId);
            Optional.ofNullable(la.optJSONObject("neighborhood")).ifPresent(neighborhood -> {
                Optional.ofNullable(neighborhood.optString("id")).ifPresent(v -> {
                    columns3.append(",neighborhood_id");
                    params3.add(v);
                });
                Optional.ofNullable(neighborhood.optString("name")).ifPresent(v -> {
                    columns3.append(",neighborhood_name");
                    params3.add(v);
                });
            });
            Optional.ofNullable(la.optJSONObject("city")).ifPresent(city -> {
                Optional.ofNullable(city.optString("id")).ifPresent(v -> {
                    columns3.append(",city_id");
                    params3.add(v);
                });
                Optional.ofNullable(city.optString("name")).ifPresent(v -> {
                    columns3.append(",city_name");
                    params3.add(v);
                });
            });
            Optional.ofNullable(la.optJSONObject("metro")).ifPresent(metro -> {
                Optional.ofNullable(metro.optString("id")).ifPresent(v -> {
                    columns3.append(",metro_id");
                    params3.add(v);
                });
                Optional.ofNullable(metro.optString("name")).ifPresent(v -> {
                    columns3.append(",metro_name");
                    params3.add(v);
                });
            });
            Optional.ofNullable(la.optJSONObject("region")).ifPresent(region -> {
                Optional.ofNullable(region.optString("id")).ifPresent(v -> {
                    columns3.append(",region_id");
                    params3.add(v);
                });
                Optional.ofNullable(region.optString("name")).ifPresent(v -> {
                    columns3.append(",region_name");
                    params3.add(v);
                });
            });
            Optional.ofNullable(la.optJSONObject("airport")).ifPresent(airport -> {
                Optional.ofNullable(airport.optString("id")).ifPresent(v -> {
                    columns3.append(",airport_id");
                    params3.add(v);
                });
                Optional.ofNullable(airport.optString("code")).ifPresent(v -> {
                    columns3.append(",airport_code");
                    params3.add(v);
                });
                Optional.ofNullable(airport.optString("name")).ifPresent(v -> {
                    columns3.append(",airport_name");
                    params3.add(v);
                });
                Optional.ofNullable(airport.optString("distance")).ifPresent(v -> {
                    columns3.append(",airport_distance");
                    params3.add(v);
                });
                Optional.ofNullable(airport.optString("unit")).ifPresent(v -> {
                    columns3.append(",airport_unit");
                    params3.add(v);
                });
            });
            Optional.ofNullable(la.optJSONObject("distanceFromCityCenter")).ifPresent(region -> {
                Optional.ofNullable(region.optString("distance")).ifPresent(v -> {
                    columns3.append(",distance_from_city_center_distance");
                    params3.add(v);
                });
                Optional.ofNullable(region.optString("unit")).ifPresent(v -> {
                    columns3.append(",distance_from_city_center_unit");
                    params3.add(v);
                });
            });
            String values3 = params3.stream().map(a -> "?").collect(Collectors.joining(","));
            String query3 = String.format(queryTemp, "gaia_location", columns3, values3);
            try (PreparedStatement preparedStatement = conn.prepareStatement(query3)) {
                for (int j = 0; j < params3.size(); j++) {
                    String value = params3.get(j);
                    int index = j + 1;
                    preparedStatement.setString(index, String.valueOf(value));
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}

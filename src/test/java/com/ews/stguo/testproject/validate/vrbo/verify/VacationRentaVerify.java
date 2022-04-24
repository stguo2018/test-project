package com.ews.stguo.testproject.validate.vrbo.verify;

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
public class VacationRentaVerify extends SDPVerify {

    @Test
    public void test01() throws Exception {
        verify("VacationRental", "sddp-feed-control-file (2).csv");
    }

    @Override
    protected void dataStorage(Connection conn, JSONObject data, String ecomId, int fileIndex) throws Exception {
        String queryTemp = "insert into %s(%s) values(%s)";
        StringBuilder columns = new StringBuilder();
        List<Object> params = new ArrayList<>();
        columns.append("ecom_id");
        params.add(ecomId);
        Optional.ofNullable(data.optJSONObject("propertySize")).ifPresent(o -> {
            Optional.ofNullable(o.optInt("measurement")).ifPresent(v -> {
                columns.append(",measurement");
                params.add(v);
            });
            Optional.ofNullable(o.optString("units")).ifPresent(v -> {
                columns.append(",units");
                params.add(v);
            });
        });
        Optional.ofNullable(data.optInt("maxOccupancy")).ifPresent(v -> {
            columns.append(",max_occupancy");
            params.add(v);
        });
        Optional.ofNullable(data.optJSONObject("bathrooms")).ifPresent(o -> {
            Optional.ofNullable(o.optInt("numberOfBathrooms")).ifPresent(v -> {
                columns.append(",number_of_bathrooms");
                params.add(v);
            });
        });
        Optional.ofNullable(data.optJSONObject("bedrooms")).ifPresent(o -> {
            Optional.ofNullable(o.optInt("numberOfBedrooms")).ifPresent(v -> {
                columns.append(",number_of_bedrooms");
                params.add(v);
            });
        });
        Optional.ofNullable(data.optJSONObject("houseRules")).ifPresent(o1 -> {
            Optional.ofNullable(o1.optJSONObject("partyOrEventRules")).ifPresent(o -> {
                Optional.ofNullable(o.optBoolean("partiesOrEventsPermitted")).ifPresent(v -> {
                    columns.append(",party_or_event_rules");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("ownerPartyFreeText")).ifPresent(v -> {
                    columns.append(",party_or_event_text");
                    params.add(v);
                });
            });
            Optional.ofNullable(o1.optJSONObject("smokingRules")).ifPresent(o -> {
                Optional.ofNullable(o.optBoolean("smokingPermitted")).ifPresent(v -> {
                    columns.append(",smoking_rules");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("ownerSmokingFreeText")).ifPresent(v -> {
                    columns.append(",smoking_rules_text");
                    params.add(v);
                });
            });
            Optional.ofNullable(o1.optJSONObject("petRules")).ifPresent(o -> {
                Optional.ofNullable(o.optBoolean("petsPermitted")).ifPresent(v -> {
                    columns.append(",pet_rules");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("ownerPetsFreeText")).ifPresent(v -> {
                    columns.append(",pet_rules_text");
                    params.add(v);
                });
            });
            Optional.ofNullable(o1.optJSONObject("childRules")).ifPresent(o -> {
                Optional.ofNullable(o.optBoolean("childrenPermitted")).ifPresent(v -> {
                    columns.append(",child_rules");
                    params.add(v);
                });
                Optional.ofNullable(o.optString("ownerChildrenFreeText")).ifPresent(v -> {
                    columns.append(",child_rules_text");
                    params.add(v);
                });
            });
        });
        Optional.ofNullable(data.optString("propertyRegistryNumber")).ifPresent(v -> {
            columns.append(",property_registry_number");
            params.add(v);
        });
        int hostLanguagesNumber = Optional.ofNullable(data.optJSONArray("hostLanguages")).map(hl -> hl.length()).orElse(0);
        columns.append(",host_languages_number");
        params.add(hostLanguagesNumber);
        Optional.ofNullable(data.optJSONObject("propertyManager")).ifPresent(o -> {
            Optional.ofNullable(o.optString("name")).ifPresent(v -> {
                columns.append(",property_manager_name");
                params.add(v);
            });
            Optional.ofNullable(o.optString("photoURL")).ifPresent(v -> {
                columns.append(",property_manager_photo");
                params.add(v);
            });
        });
        columns.append(",file_index");
        params.add(String.valueOf(fileIndex));
        String values = params.stream().map(a -> "?").collect(Collectors.joining(","));
        String query = String.format(queryTemp, "vacation_rental", columns, values);
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                int index = i + 1;
                preparedStatement.setObject(index, params.get(i));
            }
            preparedStatement.executeUpdate();
        }

    }


}

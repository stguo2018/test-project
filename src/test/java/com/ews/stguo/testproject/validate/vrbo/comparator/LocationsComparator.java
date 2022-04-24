package com.ews.stguo.testproject.validate.vrbo.comparator;

import com.ews.stguo.testproject.utils.text.TrimUtils;
import com.ews.stguo.testproject.validate.vrbo.model.locations.LocationsModel;
import com.ews.stguo.testproject.validate.vrbo.model.summary.GeoLocation;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS1;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS2;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.COUNTRY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLATITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLONGITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONOBFUSCATED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.POSTALCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROVINCE;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocationsComparator extends VrboComparator<LocationsModel> {

    public LocationsComparator(Set<Integer> hotelIds) {
        super(hotelIds);
    }

    @Override
    public String getFileType() {
        return "Locations";
    }

    @Override
    public Class<LocationsModel> getClazz() {
        return LocationsModel.class;
    }

    @Override
    public String getId(LocationsModel data) {
        return data.getPropertyId().getExpedia();
    }

    @Override
    public void compareData(LocationsModel model, Optional<JSONObject> response) {
        Optional<JSONObject> adContent = response.map(rs -> rs.optJSONObject("adContent"));
        Optional<JSONObject> headline = adContent.map(ac -> ac.optJSONObject("headline"));
        String propertyName = headline.map(hl -> hl.optJSONArray("texts")).map(ts -> {
            if (ts.length() > 0) {
                for (int i = 0; i < ts.length(); i++) {
                    Optional<JSONObject> text = Optional.ofNullable(ts.optJSONObject(i));
                    String locale = text.map(t -> t.optString("locale")).map(TrimUtils::trim).orElse(null);
                    if ("en".equalsIgnoreCase(locale)) {
                        return text.map(t -> t.optString("content")).map(TrimUtils::trim).map(String::toLowerCase).orElse(null);
                    }
                }
            }
            return null;
        }).orElse(null);
        if (Objects.equals(propertyName, model.getPropertyName().toLowerCase()) || (propertyName == null && model.getPropertyName().equals(""))) {
            updateCounter(PROPERTYNAME);
        }

        Optional<JSONObject> location = response.map(rs -> rs.optJSONObject("location"));
        Optional<JSONObject> address = location.map(l -> l.optJSONObject("address"));
        String address1 = address.map(a -> a.optString("address1")).map(TrimUtils::trim).orElse(null);
        String address2 = address.map(a -> a.optString("address2")).map(TrimUtils::trim).orElse(null);
        String city = address.map(a -> a.optString("city")).map(TrimUtils::trim).orElse(null);
        String province = address.map(a -> a.optString("stateProvince")).map(TrimUtils::trim).orElse(null);
        String country = address.map(a -> a.optString("country")).map(TrimUtils::trim).orElse(null);
        String postalCode = address.map(a -> a.optString("postalCode")).map(TrimUtils::trim).orElse(null);
        if (Objects.equals(address1, model.getAddress1()) || (address1 == null && model.getAddress1().equals(""))) {
            updateCounter(ADDRESS1);
        }
        if (Objects.equals(address2, model.getAddress2()) || (address2 == null && model.getAddress2().equals(""))) {
            updateCounter(ADDRESS2);
        }
        if (Objects.equals(city, model.getCity()) || (city == null && model.getCity().equals(""))) {
            updateCounter(CITY);
        }
        if (Objects.equals(province, model.getProvince()) || (province == null && model.getProvince().equals(""))) {
            updateCounter(PROVINCE);
        }
        if (country != null && model.getCountry() != null && model.getCountry().equalsIgnoreCase(new Locale("", country).getDisplayCountry())) {
            updateCounter(COUNTRY);
        }
        if (Objects.equals(postalCode, model.getPostalCode()) || (postalCode == null && model.getPostalCode().equals(""))) {
            updateCounter(POSTALCODE);
        }

        Optional<JSONObject> geoLocation = location.map(l -> l.optJSONObject("latLng"));
        Boolean showExactLocation = location.map(l -> l.optBoolean("showExactLocation")).orElse(null);
        Boolean obfuscated = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::isObfuscated).orElse(null);
        if (showExactLocation == null && obfuscated == null) {
            updateCounter(GEOLOCATIONOBFUSCATED);
        }
        if (showExactLocation != null && obfuscated != null && !showExactLocation == obfuscated) {
            updateCounter(GEOLOCATIONOBFUSCATED);
        } else {
            System.out.println();
        }
        Double latitude = geoLocation.map(gl -> gl.optDouble("latitude")).orElse(null);
        Double longitude = geoLocation.map(gl -> gl.optDouble("longitude")).orElse(null);
        String fLatitude = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::getLatitude).orElse(null);
        String fLongitude = Optional.ofNullable(model.getGeoLocation()).map(GeoLocation::getLongitude).orElse(null);
        if (latitude == null && fLatitude == null) {
            updateCounter(GEOLOCATIONLATITUDE);
        }
        if (latitude != null && fLatitude != null && new BigDecimal(String.valueOf(latitude)).setScale(3, RoundingMode.HALF_UP).compareTo(new BigDecimal(String.valueOf(fLatitude))) == 0) {
            updateCounter(GEOLOCATIONLATITUDE);
        }
        if (longitude == null && fLongitude == null) {
            updateCounter(GEOLOCATIONLONGITUDE);
        }
        if (longitude != null && fLongitude != null && new BigDecimal(String.valueOf(longitude)).setScale(3, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(String.valueOf(fLongitude))) == 0) {
            updateCounter(GEOLOCATIONLONGITUDE);
        }
    }

    @Override
    public void recordNoMatchAsExcel() {

    }

}

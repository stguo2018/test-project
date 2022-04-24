package com.ews.stguo.testproject.validate.vrbo.validator;

import com.ews.stguo.testproject.validate.vrbo.model.locations.LocationsModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS1;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.ADDRESS2;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AIRPORTCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AIRPORTDISTANCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AIRPORTDISTANCEUNIT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AIRPORTID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.AIRPORTNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITYID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.CITYNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.COUNTRY;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.DISTANCEFROMCITYCENTERDISTANCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.DISTANCEFROMCITYCENTERDISTANCEUNIT;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.EXPEDIAID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLATITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONLONGITUDE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.GEOLOCATIONOBFUSCATED;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.HCOMID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.METROID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.METRONAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NEIGHBORHOODID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.NEIGHBORHOODNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PHONEAREACODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PHONECOUNTRYCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PHONEEXTENSION;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PHONENUMBER;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PHONETYPE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.POSTALCODE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROPERTYNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.PROVINCE;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REGIONID;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.REGIONNAME;
import static com.ews.stguo.testproject.validate.vrbo.model.ValidateConstants.VRBOID;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocationsValidator extends VrboValidator<LocationsModel> {

    public LocationsValidator(Set<Integer> hotelIds) {
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
    public void analyse(int index, LocationsModel model) {
        if (model.getPropertyId() != null) {
            if (StringUtils.isNotBlank(model.getPropertyId().getExpedia())) {
                hotelIds.remove(Integer.parseInt(model.getPropertyId().getExpedia()));
                updateCounter(EXPEDIAID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getHcom())) {
                updateCounter(HCOMID);
            }
            if (StringUtils.isNotBlank(model.getPropertyId().getVrbo())) {
                updateCounter(VRBOID);
            }
        }

        if (StringUtils.isNotBlank(model.getPropertyName())) {
            updateCounter(PROPERTYNAME);
        }
        if (StringUtils.isNotBlank(model.getAddress1())) {
            updateCounter(ADDRESS1);
        }
        if (StringUtils.isNotBlank(model.getAddress2())) {
            updateCounter(ADDRESS2);
        }
        if (StringUtils.isNotBlank(model.getCity())) {
            updateCounter(CITY);
        }
        if (StringUtils.isNotBlank(model.getProvince())) {
            updateCounter(PROVINCE);
        }
        if (StringUtils.isNotBlank(model.getCountry())) {
            updateCounter(COUNTRY);
        }
        if (StringUtils.isNotBlank(model.getPostalCode())) {
            updateCounter(POSTALCODE);
        }

        if (CollectionUtils.isNotEmpty(model.getPhone())) {
            if (StringUtils.isNotBlank(model.getPhone().get(0).getType())) {
                updateCounter(PHONETYPE);
            }
            if (StringUtils.isNotBlank(model.getPhone().get(0).getCountryCode())) {
                updateCounter(PHONECOUNTRYCODE);
            }
            if (StringUtils.isNotBlank(model.getPhone().get(0).getAreaCode())) {
                updateCounter(PHONEAREACODE);
            }
            if (StringUtils.isNotBlank(model.getPhone().get(0).getNumber())) {
                updateCounter(PHONENUMBER);
            }
            if (StringUtils.isNotBlank(model.getPhone().get(0).getExtension())) {
                updateCounter(PHONEEXTENSION);
            }
        }

        if (model.getGeoLocation() != null) {
            if (StringUtils.isNotBlank(model.getGeoLocation().getLatitude())) {
                updateCounter(GEOLOCATIONLATITUDE);
            }
            if (StringUtils.isNotBlank(model.getGeoLocation().getLongitude())) {
                updateCounter(GEOLOCATIONLONGITUDE);
            }
            updateCounter(GEOLOCATIONOBFUSCATED);
        }

        if (model.getLocationAttribute() != null) {
            if (model.getLocationAttribute().getNeighborhood() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getNeighborhood().getId())) {
                    updateCounter(NEIGHBORHOODID);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getNeighborhood().getName())) {
                    updateCounter(NEIGHBORHOODNAME);
                }
            }
            if (model.getLocationAttribute().getCity() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getCity().getId())) {
                    updateCounter(CITYID);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getCity().getName())) {
                    updateCounter(CITYNAME);
                }
            }
            if (model.getLocationAttribute().getMetro() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getMetro().getId())) {
                    updateCounter(METROID);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getMetro().getName())) {
                    updateCounter(METRONAME);
                }
            }
            if (model.getLocationAttribute().getRegion() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getRegion().getId())) {
                    updateCounter(REGIONID);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getRegion().getName())) {
                    updateCounter(REGIONNAME);
                }
            }
            if (model.getLocationAttribute().getAirport() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getAirport().getId())) {
                    updateCounter(AIRPORTID);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getAirport().getCode())) {
                    updateCounter(AIRPORTCODE);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getAirport().getName())) {
                    updateCounter(AIRPORTNAME);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getAirport().getDistance())) {
                    updateCounter(AIRPORTDISTANCE);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getAirport().getUnit())) {
                    updateCounter(AIRPORTDISTANCEUNIT);
                }
            }
            if (model.getLocationAttribute().getDistanceFromCityCenter() != null) {
                if (StringUtils.isNotBlank(model.getLocationAttribute().getDistanceFromCityCenter().getDistance())) {
                    updateCounter(DISTANCEFROMCITYCENTERDISTANCE);
                }
                if (StringUtils.isNotBlank(model.getLocationAttribute().getDistanceFromCityCenter().getUnit())) {
                    updateCounter(DISTANCEFROMCITYCENTERDISTANCEUNIT);
                }
            }
        }

    }
}

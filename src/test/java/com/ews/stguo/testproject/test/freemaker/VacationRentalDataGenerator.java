package com.ews.stguo.testproject.test.freemaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class VacationRentalDataGenerator {

    public static Map<String, Object> getVacationRentalDataMap() throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        List<Listing> listings = new ArrayList<>();

        Listing listing = new Listing();
        listing.setEcomHotelId("23423432");
        listing.setHcomHotelId("34523433");
        listing.setVrboPropertyId("1.2.3");
        listing.setPropertyRegistryNumber("STR-OPLI-19-001813");
        listing.setPropertyBadge(Arrays.asList("A", "BB"));
        listing.setPropertySize(55500D, PropertySizeUnits.SQUARE_FEET);
        listing.setMinimumStay(2000);
        listing.setMaximumStay(6000);
        listing.setMaxOccupancy(9000);
        listing.setBathrooms(5, 1);
        listing.setBedrooms(15, 10, 2, Arrays.asList("King","Queen"));
        listing.setNumberOfSeatedDiners(10);
        listing.setPartyOrEventRules(true, "Party is allowed.");
        listing.setSmokingRules(false, "Non-Smoking.");
//        listing.setPetRules(true, "Pets is allowed.");
        listing.setPetRules(true, "aaa");
        listing.setChildRules(true, "Children friendly.");
        listing.setOwnerPhotoURL("ownerPhotoURL");
        listing.setHostLanguages(Arrays.asList("English", "Chinese"));
        listing.setPropertyContact("K.sfsd");
        listing.setPropertyManager("sdfkas", "URL");
        listings.add(listing);

        listing = new Listing();
        listing.setEcomHotelId("23423432");
        listing.setHcomHotelId("34523433");
        listing.setVrboPropertyId("1.2.3");
        listing.setPropertySize(555D, PropertySizeUnits.SQUARE_FEET);
        listing.setMinimumStay(2);
        listing.setMaximumStay(6);
        listing.setMaxOccupancy(9);
        listing.setBathrooms(5, null);
        listing.setBedrooms(15, 10, 2, Arrays.asList("King","Queen"));
        listing.setNumberOfSeatedDiners(10);
        listings.add(listing);

        listing = new Listing();
        listing.setEcomHotelId("23423432");
        listing.setHcomHotelId("34523433");
        listing.setVrboPropertyId("1.2.3");
        listing.setPropertyRegistryNumber("STR-OPLI-19-001813");
        listing.setPropertyBadge(Arrays.asList("A", "BB"));
        listing.setPropertySize(555.21D, PropertySizeUnits.SQUARE_FEET);
        listing.setMinimumStay(2);
        listing.setMaximumStay(6);
        listing.setMaxOccupancy(9);
        listing.setBathrooms(5, 1);
        listing.setBedrooms(15, 10, 2, Arrays.asList("King","Queen"));
        listing.setNumberOfSeatedDiners(10);
        listing.setPartyOrEventRules(true, "Party is allowed.");
        listing.setSmokingRules(false, "Non-Smoking.");
        listing.setPetRules(true, "Pets \" is allowed.");
        listing.setChildRules(true, "Children friendly.");
        listing.setOwnerPhotoURL("ownerPhotoURL");
        listing.setHostLanguages(Arrays.asList("English", "Chinese"));
        listing.setPropertyContact("K.sfsd");
        listing.setPropertyManager("sdfkas", "URL");
        listings.add(listing);

        listing = new Listing();
        listing.setEcomHotelId("23423432");
        listing.setHcomHotelId("34523433");
        listing.setVrboPropertyId("1.2.3");
        listing.setPropertyRegistryNumber("STR-OPLI-19-001813");
        listing.setPropertyBadge(Arrays.asList("A", "BB"));
        listing.setPropertySize(555.21D, PropertySizeUnits.SQUARE_FEET);
        listing.setMinimumStay(2);
        listing.setMaximumStay(6);
        listing.setMaxOccupancy(9);
        listing.setBathrooms(5, 1);
        listing.setBedrooms(15, 10, 2, Arrays.asList("King","Queen"));
        listing.setNumberOfSeatedDiners(10);
        listing.setPartyOrEventRules(true, "Party is allowed.");
        listing.setSmokingRules(false, "Non-Smoking.");
        listing.setPetRules(true, "Pets is allowed.");
        listing.setChildRules(true, "Children friendly.");
        listing.setOwnerPhotoURL("ownerPhotoURL");
        listing.setHostLanguages(Arrays.asList("English", "Chinese"));
        listing.setPropertyContact("K.sfsd");
        listing.setPropertyManager("sdfkas", "URL");
        listings.add(listing);

        dataMap.put("listings", listings);
        dataMap.put("generateBody", true);
        return dataMap;
    }

    public static class Listing {
        private String ecomHotelId;
        private String hcomHotelId;
        private String vrboPropertyId;
        private String propertyRegistryNumber;
        private List<String> propertyBadge;
        private PropertySize propertySize;
        private Integer minimumStay;
        private Integer maximumStay;
        private Integer maxOccupancy;
        private Bathrooms bathrooms;
        private Bedrooms bedrooms;
        private Integer numberOfSeatedDiners;
        private HouseRules houseRules;
        private String ownerPhotoURL;
        private List<String> hostLanguages;
        private String propertyContact;
        private PropertyManager propertyManager;

        public String getEcomHotelId() {
            return ecomHotelId;
        }

        public void setEcomHotelId(String ecomHotelId) {
            this.ecomHotelId = ecomHotelId;
        }

        public String getHcomHotelId() {
            return hcomHotelId;
        }

        public void setHcomHotelId(String hcomHotelId) {
            this.hcomHotelId = hcomHotelId;
        }

        public String getVrboPropertyId() {
            return vrboPropertyId;
        }

        public void setVrboPropertyId(String vrboPropertyId) {
            this.vrboPropertyId = vrboPropertyId;
        }

        public String getPropertyRegistryNumber() {
            return propertyRegistryNumber;
        }

        public void setPropertyRegistryNumber(String propertyRegistryNumber) {
            this.propertyRegistryNumber = propertyRegistryNumber;
        }

        public List<String> getPropertyBadge() {
            return propertyBadge;
        }

        public void setPropertyBadge(List<String> propertyBadge) {
            this.propertyBadge = propertyBadge;
        }

        public PropertySize getPropertySize() {
            return propertySize;
        }

        public void setPropertySize(Double measurement, PropertySizeUnits units) {
            this.propertySize = new PropertySize();
            this.propertySize.setMeasurement(measurement);
            this.propertySize.setUnits(units);
        }

        public Integer getMinimumStay() {
            return minimumStay;
        }

        public void setMinimumStay(Integer minimumStay) {
            this.minimumStay = minimumStay;
        }

        public Integer getMaximumStay() {
            return maximumStay;
        }

        public void setMaximumStay(Integer maximumStay) {
            this.maximumStay = maximumStay;
        }

        public Integer getMaxOccupancy() {
            return maxOccupancy;
        }

        public void setMaxOccupancy(Integer maxOccupancy) {
            this.maxOccupancy = maxOccupancy;
        }

        public Bathrooms getBathrooms() {
            return bathrooms;
        }

        public void setBathrooms(Integer numberOfBathrooms, Integer numberOfToiletOnlyRooms) {
            this.bathrooms = new Bathrooms();
            this.bathrooms.setNumberOfBathrooms(numberOfBathrooms);
            this.bathrooms.setNumberOfToiletOnlyRooms(numberOfToiletOnlyRooms);
        }

        public Bedrooms getBedrooms() {
            return bedrooms;
        }

        public void setBedrooms(Integer numberOfBedrooms, Integer maxNumberOfSleepers,
                                Integer maxNumberOfSleepersInBed, List<String> bedTypes) {
            this.bedrooms = new Bedrooms();
            this.bedrooms.setNumberOfBedrooms(numberOfBedrooms);
            this.bedrooms.setMaxNumberOfSleepers(maxNumberOfSleepers);
            this.bedrooms.setMaxNumberSleepersInBeds(maxNumberOfSleepersInBed);
            this.bedrooms.setBedTypes(bedTypes);
        }

        public Integer getNumberOfSeatedDiners() {
            return numberOfSeatedDiners;
        }

        public void setNumberOfSeatedDiners(Integer numberOfSeatedDiners) {
            this.numberOfSeatedDiners = numberOfSeatedDiners;
        }

        public HouseRules getHouseRules() {
            return houseRules;
        }

        public void setPartyOrEventRules(boolean permitted, String freeText) {
            if (this.houseRules == null) {
                this.houseRules = new HouseRules();
            }
            PartyOrEventRules partyOrEventRules = new PartyOrEventRules();
            partyOrEventRules.setPartiesOrEventsPermitted(permitted);
            partyOrEventRules.setOwnerPartyFreeText(freeText);
            houseRules.setPartyOrEventRules(partyOrEventRules);
        }

        public void setSmokingRules(boolean permitted, String freeText) {
            if (this.houseRules == null) {
                this.houseRules = new HouseRules();
            }
            SmokingRules smokingRules = new SmokingRules();
            smokingRules.setSmokingPermitted(permitted);
            smokingRules.setOwnerSmokingFreeText(freeText);
            houseRules.setSmokingRules(smokingRules);
        }

        public void setPetRules(boolean permitted, String freeText) {
            if (this.houseRules == null) {
                this.houseRules = new HouseRules();
            }
            PetRules petRules = new PetRules();
            petRules.setPetsPermitted(permitted);
            petRules.setOwnerPetsFreeText(freeText);
            houseRules.setPetRules(petRules);
        }

        public void setChildRules(boolean permitted, String freeText) {
            if (this.houseRules == null) {
                this.houseRules = new HouseRules();
            }
            ChildRules childRules = new ChildRules();
            childRules.setChildrenPermitted(permitted);
            childRules.setOwnerChildrenFreeText(freeText);
            houseRules.setChildRules(childRules);
        }

        public String getOwnerPhotoURL() {
            return ownerPhotoURL;
        }

        public void setOwnerPhotoURL(String ownerPhotoURL) {
            this.ownerPhotoURL = ownerPhotoURL;
        }

        public List<String> getHostLanguages() {
            return hostLanguages;
        }

        public void setHostLanguages(List<String> hostLanguages) {
            this.hostLanguages = hostLanguages;
        }

        public String getPropertyContact() {
            return propertyContact;
        }

        public void setPropertyContact(String propertyContact) {
            this.propertyContact = propertyContact;
        }

        public PropertyManager getPropertyManager() {
            return propertyManager;
        }

        public void setPropertyManager(String name, String photoURL) {
            this.propertyManager = new PropertyManager();
            this.propertyManager.setName(name);
            this.propertyManager.setPhotoURL(photoURL);
        }
    }

    public static class PropertySize {
        private double measurement;
        private PropertySizeUnits units;

        public double getMeasurement() {
            return measurement;
        }

        public void setMeasurement(double measurement) {
            this.measurement = measurement;
        }

        public PropertySizeUnits getUnits() {
            return units;
        }

        public void setUnits(PropertySizeUnits units) {
            this.units = units;
        }
    }

    public enum PropertySizeUnits {
        SQUARE_FEET,
        SQUARE_METERS
    }

    public static class Bathrooms {
        private Integer numberOfBathrooms;
        private Integer numberOfToiletOnlyRooms;

        public Integer getNumberOfBathrooms() {
            return numberOfBathrooms;
        }

        public void setNumberOfBathrooms(Integer numberOfBathrooms) {
            this.numberOfBathrooms = numberOfBathrooms;
        }

        public Integer getNumberOfToiletOnlyRooms() {
            return numberOfToiletOnlyRooms;
        }

        public void setNumberOfToiletOnlyRooms(Integer numberOfToiletOnlyRooms) {
            this.numberOfToiletOnlyRooms = numberOfToiletOnlyRooms;
        }
    }

    public static class Bedrooms {
        private Integer numberOfBedrooms;
        private Integer maxNumberOfSleepers;
        private Integer maxNumberSleepersInBeds;
        private List<String> bedTypes;

        public Integer getNumberOfBedrooms() {
            return numberOfBedrooms;
        }

        public void setNumberOfBedrooms(Integer numberOfBedrooms) {
            this.numberOfBedrooms = numberOfBedrooms;
        }

        public Integer getMaxNumberOfSleepers() {
            return maxNumberOfSleepers;
        }

        public void setMaxNumberOfSleepers(Integer maxNumberOfSleepers) {
            this.maxNumberOfSleepers = maxNumberOfSleepers;
        }

        public Integer getMaxNumberSleepersInBeds() {
            return maxNumberSleepersInBeds;
        }

        public void setMaxNumberSleepersInBeds(Integer maxNumberSleepersInBeds) {
            this.maxNumberSleepersInBeds = maxNumberSleepersInBeds;
        }

        public List<String> getBedTypes() {
            return bedTypes;
        }

        public void setBedTypes(List<String> bedTypes) {
            this.bedTypes = bedTypes;
        }
    }

    public static class HouseRules {
        private PartyOrEventRules partyOrEventRules;
        private SmokingRules smokingRules;
        private PetRules petRules;
        private ChildRules childRules;

        public PartyOrEventRules getPartyOrEventRules() {
            return partyOrEventRules;
        }

        public void setPartyOrEventRules(PartyOrEventRules partyOrEventRules) {
            this.partyOrEventRules = partyOrEventRules;
        }

        public SmokingRules getSmokingRules() {
            return smokingRules;
        }

        public void setSmokingRules(SmokingRules smokingRules) {
            this.smokingRules = smokingRules;
        }

        public PetRules getPetRules() {
            return petRules;
        }

        public void setPetRules(PetRules petRules) {
            this.petRules = petRules;
        }

        public ChildRules getChildRules() {
            return childRules;
        }

        public void setChildRules(ChildRules childRules) {
            this.childRules = childRules;
        }
    }

    public static class PartyOrEventRules {
        private boolean partiesOrEventsPermitted;
        private String ownerPartyFreeText;

        public boolean isPartiesOrEventsPermitted() {
            return partiesOrEventsPermitted;
        }

        public void setPartiesOrEventsPermitted(boolean partiesOrEventsPermitted) {
            this.partiesOrEventsPermitted = partiesOrEventsPermitted;
        }

        public String getOwnerPartyFreeText() {
            return ownerPartyFreeText;
        }

        public void setOwnerPartyFreeText(String ownerPartyFreeText) {
            this.ownerPartyFreeText = ownerPartyFreeText;
        }
    }

    public static class SmokingRules {
        private boolean smokingPermitted;
        private String ownerSmokingFreeText;

        public boolean isSmokingPermitted() {
            return smokingPermitted;
        }

        public void setSmokingPermitted(boolean smokingPermitted) {
            this.smokingPermitted = smokingPermitted;
        }

        public String getOwnerSmokingFreeText() {
            return ownerSmokingFreeText;
        }

        public void setOwnerSmokingFreeText(String ownerSmokingFreeText) {
            this.ownerSmokingFreeText = ownerSmokingFreeText;
        }
    }

    public static class PetRules {
        private boolean petsPermitted;
        private String ownerPetsFreeText;

        public boolean isPetsPermitted() {
            return petsPermitted;
        }

        public void setPetsPermitted(boolean petsPermitted) {
            this.petsPermitted = petsPermitted;
        }

        public String getOwnerPetsFreeText() {
            return ownerPetsFreeText;
        }

        public void setOwnerPetsFreeText(String ownerPetsFreeText) {
            this.ownerPetsFreeText = ownerPetsFreeText;
        }
    }

    public static class ChildRules {
        private boolean childrenPermitted;
        private String ownerChildrenFreeText;

        public boolean isChildrenPermitted() {
            return childrenPermitted;
        }

        public void setChildrenPermitted(boolean childrenPermitted) {
            this.childrenPermitted = childrenPermitted;
        }

        public String getOwnerChildrenFreeText() {
            return ownerChildrenFreeText;
        }

        public void setOwnerChildrenFreeText(String ownerChildrenFreeText) {
            this.ownerChildrenFreeText = ownerChildrenFreeText;
        }
    }

    public static class PropertyManager {
        private String name;
        private String photoURL;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhotoURL() {
            return photoURL;
        }

        public void setPhotoURL(String photoURL) {
            this.photoURL = photoURL;
        }
    }

}

package com.ews.stguo.testproject.validate.vrbo.model.vacationrental;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class HouseRules {

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

    public class PartyOrEventRules {
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

    public class SmokingRules {
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

    public class PetRules {
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

    public class ChildRules {
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

}

package com.ews.stguo.testproject.validate.vrbo.model.locations;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class LocationAttribute {

    private Neighborhood neighborhood;
    private City city;
    private Metro metro;
    private Region region;
    private Airport airport;
    private DistanceFromCityCenter distanceFromCityCenter;

    public Neighborhood getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(Neighborhood neighborhood) {
        this.neighborhood = neighborhood;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Metro getMetro() {
        return metro;
    }

    public void setMetro(Metro metro) {
        this.metro = metro;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public DistanceFromCityCenter getDistanceFromCityCenter() {
        return distanceFromCityCenter;
    }

    public void setDistanceFromCityCenter(DistanceFromCityCenter distanceFromCityCenter) {
        this.distanceFromCityCenter = distanceFromCityCenter;
    }
}

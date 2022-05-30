package com.ews.stguo.testproject.designpatterns.builder;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public interface Builder {

    void setCarType(CarType type);
    void setSeats(int seats);
    void setEngine(Engine engine);
    void setTransmission(Transmission transmission);
    void setTripComputer(TripComputer tripComputer);
    void setGPSNavigator(GPSNavigator gpsNavigator);

}

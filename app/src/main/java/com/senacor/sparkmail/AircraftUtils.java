package com.senacor.sparkmail;

import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public final class AircraftUtils {

    public static FlightController getFlightController() {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();

        if (aircraft != null) {
            return aircraft.getFlightController();
        }
        return null;
    }

}

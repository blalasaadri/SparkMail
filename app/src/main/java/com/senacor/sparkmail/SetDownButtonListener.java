package com.senacor.sparkmail;

import android.view.View;

import dji.sdk.flightcontroller.FlightController;

import static com.senacor.sparkmail.AircraftUtils.getFlightController;
import static com.senacor.sparkmail.SystemUtils.showShortToast;

class SetDownButtonListener implements View.OnClickListener {
    private MainActivity mainActivity;

    SetDownButtonListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        showShortToast("Set down", mainActivity);

        FlightController flightController = getFlightController();
        if (flightController == null) {
            showShortToast("Error while setting down :-(", mainActivity);
        } else {
            flightController.startLanding(djiError -> {
                if (djiError == null) {
                    showShortToast("Success!", mainActivity);
                } else {
                    showShortToast("Miserable failure :-( because: " + djiError.getDescription(), mainActivity);
                }
            });
        }
    }
}

package com.senacor.sparkmail;

import android.view.View;

import dji.sdk.flightcontroller.FlightController;

import static com.senacor.sparkmail.AircraftUtils.getFlightController;
import static com.senacor.sparkmail.SystemUtils.showShortToast;

class TakeOffButtonListener implements View.OnClickListener {
    private MainActivity mainActivity;

    public TakeOffButtonListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        showShortToast("Take off", mainActivity);

        FlightController flightController = getFlightController();
        if (flightController == null) {
            showShortToast("Error while taking off :-(", mainActivity);
        } else {
            flightController.startTakeoff(djiError -> {
                if (djiError == null) {
                    showShortToast("Success!", mainActivity);
                } else {
                    showShortToast("Miserable failure :-( because: " + djiError.getDescription(), mainActivity);
                }
            });
        }
    }
}

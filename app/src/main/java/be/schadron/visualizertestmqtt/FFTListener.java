package be.schadron.visualizertestmqtt;

/**
 * An interface that is being implemented by classes which want to listen to the FFTCalculator
 */

interface FFTListener {
    void onCalculationFinished(Band[] bands);
}

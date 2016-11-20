package be.schadron.visualizertestmqtt;

import android.media.audiofx.Visualizer;

import java.util.ArrayList;

/**
 * Created by jenss on 16/11/2016.
 */

public class FFTCalculator implements Visualizer.OnDataCaptureListener {
    private static final int BACKGROUND = 0;
    private static final int FFT_CHANNELS = 32;
    private static final int LED_LEVEL = 2;
    private static final int MAX_MDB = 50000;
    private Band[] bands;
    private int captureSize;
    private float logMax;
    private Visualizer visualizer;

    private ArrayList<FFTListener> listeners;

    public FFTCalculator() {
        listeners = new ArrayList<>();

        this.bands = new Band[FFT_CHANNELS];
        for (int i = BACKGROUND; i < FFT_CHANNELS; i++) {
            if (this.bands[i] == null) {
                this.bands[i] = new Band();
            }
            this.bands[i].setLevel(MAX_MDB);
        }

        startCalculation();
    }

    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        int i;
        int j = LED_LEVEL;
        for (i = 0; i < FFT_CHANNELS; i++) {
            while (j < this.captureSize) {
                Band band = this.bands[i];
                int j2 = j + 1;
                byte b = fft[j];
                j = j2 + 1;
                band.add(b, fft[j2]);
                if (((double) ((j - 2) / LED_LEVEL)) >= Math.pow(10.0d, (double) (((float) i) * this.logMax))) {
                    break;
                }
            }
        }
        for (i = 0; i < FFT_CHANNELS; i++) {
            this.bands[i].calculate();
        }
        for (i = 0; i < listeners.size(); i++) {
            this.listeners.get(i).onCalculationFinished(this.bands);
        }
    }

    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
    }

    void stopCalculation(){
        if(this.visualizer.getEnabled()) {
            this.visualizer.setEnabled(false);
            this.visualizer.release();
        }
    }

    void startCalculation(){
        this.visualizer = new Visualizer(BACKGROUND);
        this.visualizer.setEnabled(false);
        this.visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        this.visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), false, true);
        this.captureSize = this.visualizer.getCaptureSize();
        this.logMax = ((float) Math.log10((double) (((float) this.captureSize) / 2.0f))) / 40.0f;
        this.visualizer.setEnabled(true);
    }

    void addListener(FFTListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FFTListener listener) {
        listeners.remove(listener);
    }

    public void destroyListeners() {
        listeners = new ArrayList<>();
    }
}

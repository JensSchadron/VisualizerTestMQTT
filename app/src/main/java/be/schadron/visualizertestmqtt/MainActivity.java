package be.schadron.visualizertestmqtt;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private VisualizerView visualizerView;
    private MQTTClient mqttClient;
    private FFTCalculator calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.calculator = new FFTCalculator();
        this.visualizerView = (VisualizerView) findViewById(R.id.view);
        this.mqttClient = new MQTTClient();


    }

    protected void onPause() {
        this.mqttClient.closeConnection();

        this.visualizerView.onPause();

        this.calculator.pauseCalculation();
        this.calculator.destroyListeners();

        super.onPause();
    }

    protected void onResume() {
        this.mqttClient.openConnection();

        this.visualizerView.onResume();

        this.calculator.resumeCalculation();
        this.calculator.addListener(this.visualizerView);
        this.calculator.addListener(this.mqttClient);

        super.onResume();
    }
}

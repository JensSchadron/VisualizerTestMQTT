package be.schadron.visualizertestmqtt;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {
    private VisualizerView visualizerView;
    private MQTTClient mqttClient;
    private FFTCalculator calculator;

    private boolean generalEnabled;
    private boolean mqttEnabled;

    private final static int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE);
        generalEnabled = sp.getBoolean(getString(R.string.generalSwitchState), true);
        mqttEnabled = sp.getBoolean(getString(R.string.mqttSwitchState), true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
                //TODO https://developer.android.com/training/permissions/requesting.html
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.


            }
        }


        this.calculator = new FFTCalculator();
        this.visualizerView = (VisualizerView) findViewById(R.id.spectrumView);
        this.mqttClient = new MQTTClient(mqttEnabled);

        final Switch swtGeneral = (Switch) findViewById(R.id.general);
        final Switch swtMQTT = (Switch) findViewById(R.id.mqtt);
        swtGeneral.setChecked(generalEnabled);
        swtMQTT.setChecked(mqttEnabled);

        swtGeneral.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE).edit().putBoolean(getString(R.string.generalSwitchState), enabled).apply();
                if (generalEnabled = enabled) {
                    swtMQTT.setEnabled(true);
                    changeMQTTClientStatus(mqttEnabled, false);

                    calculator.startCalculation();
                    calculator.addListener(visualizerView);
                } else {
                    swtMQTT.setEnabled(false);
                    changeMQTTClientStatus(false, false);

                    calculator.stopCalculation();
                    calculator.removeListener(visualizerView);
                }
            }
        });
        swtMQTT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                changeMQTTClientStatus(enabled, true);
            }
        });
    }

    private void changeMQTTClientStatus(boolean enabled, boolean saveStatus) {
        if (saveStatus) {
            getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE).edit().putBoolean(getString(R.string.mqttSwitchState), enabled).apply();
            mqttEnabled = enabled;
        }
        if (enabled) {
            mqttClient.openConnection(true);
            calculator.addListener(mqttClient);
        } else {
            calculator.removeListener(mqttClient);
            mqttClient.closeConnection();

        }
    }

    protected void onPause() {
        this.calculator.removeListener(this.visualizerView);

        super.onPause();
    }

    protected void onResume() {
        if (generalEnabled) {
            if (mqttEnabled) {
                this.mqttClient.openConnection(true);
                this.calculator.addListener(this.mqttClient);
            }
            this.calculator.addListener(this.visualizerView);

            this.calculator.startCalculation();
        }

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.wtf("Main", "Is it really granted?");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Log.wtf("Main", "Why are you so mean? :'(");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

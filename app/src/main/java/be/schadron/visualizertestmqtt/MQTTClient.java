package be.schadron.visualizertestmqtt;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

/**
 * This class is used to publish the FFT data on a MQTT Queue
 */

class MQTTClient implements FFTListener {
    private static final int LEDS_COUNT = 16;
    private static final int MAX_MDB = 50000;

    private final static String topic = "matrixInfo";
    private final static int qos = 0;
    private final static String broker = "tcp://192.168.1.3:1883";
    private final static String clientId = "AndroidFFTClient";
    private byte[] previousPacket;

    private MqttClient client;
    private MqttConnectOptions connOpts;

    private boolean enabled;

    MQTTClient(boolean enabled) {
        this.enabled = enabled;
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    @Override
    public void onCalculationFinished(Band[] bands) {
        byte[] byteArray = new byte[32];
        for (int i = 0; i < bands.length; i++) {
            int lvl = (bands[i].getLevel() * LEDS_COUNT) / MAX_MDB;
            if (lvl == 0) {
                byteArray[i] = 16;
            } else {
                byteArray[i] = (byte) --lvl;
            }
        }
        if (Arrays.equals(previousPacket, byteArray)) {
            System.out.println("Payload is equal, not sending packet.");
            return;
        }

        try {
            System.out.println("Publishing message: " + Arrays.toString(byteArray));
            MqttMessage message = new MqttMessage(byteArray);
            message.setQos(qos);
            client.publish(topic, message);
            System.out.println("Message published");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
            if (me.getReasonCode() == 32104) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.w("MQTTClient", "Trying to reconnect");
                        openConnection(false);
                    }
                }).start();
            }
        }

        previousPacket = byteArray;
    }

    void openConnection(boolean forceStart){
        enabled = forceStart;
        if(!client.isConnected() & enabled) {
            try {
                System.out.println("Connecting to broker: " + broker);
                client.connect(connOpts);
                System.out.println("Connected");
            } catch (MqttException me) {
                System.out.println("reason " + me.getReasonCode());
                System.out.println("msg " + me.getMessage());
                System.out.println("loc " + me.getLocalizedMessage());
                System.out.println("cause " + me.getCause());
                System.out.println("excep " + me);
                me.printStackTrace();
            }
        }
    }

    void closeConnection() {
        enabled = false;
        try {
            client.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}

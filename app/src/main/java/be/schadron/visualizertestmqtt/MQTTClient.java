package be.schadron.visualizertestmqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

/**
 * Created by jenss on 16/11/2016.
 */

public class MQTTClient implements FFTListener {
    private static final int LEDS_COUNT = 16;
    private static final int MAX_MDB = 50000;

    private final String topic = "matrixInfo";
    private final int qos = 0;
    private final String broker = "tcp://192.168.1.10:1883";
    private final String clientId = "AndroidFFTClient";

    private MqttClient client;
    private MqttConnectOptions connOpts;

    public MQTTClient() {
        try {
            client = new MqttClient(broker, clientId, null);
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
        }
    }

    public void openConnection() {
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

    public void closeConnection() {
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

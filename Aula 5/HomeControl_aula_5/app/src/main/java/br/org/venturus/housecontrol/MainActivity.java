package br.org.venturus.housecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TOPIC = "v4tech/sylvio/iot";

    private String mBrokerURL;
    private String mBrokerPort;

    // UI
    private TextView mTextTemp;
    private TextView mTempUpdate;
    private Switch mLightSwitch;
    private TextView mSwitchText;
    private ImageView mCardBackground;
    private ImageView mLightBulb;

    private MqttClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            updateActionbarSubtitle(false);
            getSupportActionBar().setLogo(R.mipmap.launcher_action);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mSwitchText = (TextView) findViewById(R.id.lamp_switch_text);
        mCardBackground = (ImageView) findViewById(R.id.card_bg);
        mLightBulb = (ImageView) findViewById(R.id.lamp);
        mLightSwitch = (Switch) findViewById(R.id.lamp_switch);

        mLightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Switch button = (Switch) view;
                if (mClient != null && mClient.isConnected()) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("seta_led", button.isChecked() ? "ligado" : "desligado");
                        mClient.publish(TOPIC, json.toString().getBytes(), 0, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MqttPersistenceException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mTextTemp = (TextView) findViewById(R.id.temperature);
        mTempUpdate = (TextView) findViewById(R.id.temp_update);
        mTextTemp.setText("-.-");
        mTempUpdate.setText("");

        updateBrokerInfo();
        setupMqtt();
        connectMqtt();
    }

    private void setupMqtt() {
        String server = "tcp://" + mBrokerURL + ":" + mBrokerPort;

        try {
            mClient = new MqttClient(server, "id_sylvio", new MemoryPersistence());
            mClient.setTimeToWait(10000);
            mClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection lost!");
                    updateActionbarSubtitle(false);
                    invalidateOptionsMenu();
                }

                @Override
                public void messageArrived(String topic, final MqttMessage message) {
                    Log.d(TAG, "Nova mensagem no tópico: " + topic + ": " + message.toString());

                    // handle message if it has content
                    if (message.getPayload().length > 0) {
                        try {
                            JSONObject json = new JSONObject(message.toString());

                            // get current temperature
                            if (json.has("temperatura")) {
                                double temp = json.getDouble("temperatura");
                                updateTemperature(temp);
                                Log.d(TAG, "Temperatura: " + temp);
                            }

                            // get lamp state
                            if (json.has("led")) {
                                String state = json.getString("led");
                                if (state.equals("ligado")) {
                                    updateSwitchState(true);
                                } else if (state.equals("desligado")) {
                                    updateSwitchState(false);
                                }
                                Log.d(TAG, "Led: " + state);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Invalid JSON.. " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (MqttException e) {
            Log.e(TAG, "Erro: " + e.toString());
            e.printStackTrace();
        }
    }

    private void connectMqtt() {
        // start connection to Broker
        if (mClient != null && !mClient.isConnected()) {
            try {
                mClient.connect();
                if (mClient.isConnected()) {
                    mClient.subscribe(TOPIC);
                    JSONObject json = new JSONObject();
                    json.put("status", 0);
                    mClient.publish(TOPIC, json.toString().getBytes(), 0, false);
                    updateActionbarSubtitle(true);
                    invalidateOptionsMenu();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void disconnectMqtt() {
        // start connection to Broker
        if (mClient != null && mClient.isConnected()) {
            try {
                mClient.disconnect();
                updateActionbarSubtitle(false);
                invalidateOptionsMenu();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_connect:
                if (item.getTitle().equals(getResources().getString(R.string.settings_connect))) {
                    item.setTitle(R.string.settings_disconnect);
                    connectMqtt();
                } else {
                    item.setTitle(R.string.settings_connect);
                    disconnectMqtt();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem connect = menu.findItem(R.id.action_connect);
        if (mClient != null && mClient.isConnected()) {
            connect.setTitle(R.string.settings_disconnect);
        } else {
            connect.setTitle(R.string.settings_connect);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateBrokerInfo();
    }

    private void updateBrokerInfo() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mBrokerURL = sharedPreferences.getString(getResources().getString(R.string.pref_key_server), "");
        mBrokerPort = sharedPreferences.getString(getResources().getString(R.string.pref_key_port), "");
    }

    private void updateActionbarSubtitle(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getSupportActionBar() != null) {
                    if (connected) {
                        getSupportActionBar().setSubtitle("Connected");
                    } else {
                        getSupportActionBar().setSubtitle("Disconnected");
                    }
                }
            }
        });
    }

    private void updateSwitchState(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    mSwitchText.setText(R.string.switch_text_on);
                    mCardBackground.setImageResource(R.drawable.bgon);
                    mLightBulb.setImageResource(R.drawable.icon_luz_on);
                    mLightSwitch.setChecked(state);
                } else {
                    mSwitchText.setText(R.string.switch_text_off);
                    mCardBackground.setImageResource(R.drawable.bgoff);
                    mLightBulb.setImageResource(R.drawable.icon_luz_off);
                    mLightSwitch.setChecked(state);
                }
            }
        });
    }

    private void updateTemperature(final double temp) {
        // update UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextTemp != null) {
                    DecimalFormat form = new DecimalFormat("0.0");
                    String tempText = getString(R.string.temperature, form.format(temp));
                    mTextTemp.setText(tempText);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String date = simpleDateFormat.format(Calendar.getInstance().getTime());
                    String update = getString(R.string.atualizacao_temp, date);
                    mTempUpdate.setText(update);
                }
            }
        });
    }
}

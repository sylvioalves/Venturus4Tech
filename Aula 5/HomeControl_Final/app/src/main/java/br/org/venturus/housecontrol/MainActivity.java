package br.org.venturus.housecontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Configurations
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

    // MQTT
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
                Switch led = (Switch) view;
                if (mClient != null && mClient.isConnected()) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("seta_led", led.isChecked() ? "ligado" : "desligado");
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

    @Override
    protected void onResume() {
        super.onResume();
        connectToServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectFromServer();
    }

    private void connectToServer() {
        if (mBrokerPort != null && !mBrokerURL.equals("")) {
            if (mClient != null && !mClient.isConnected()) {
                new ConnectTask().execute(mClient);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_config_broker, Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnectFromServer() {
        new DisconnectTask().execute(mClient);
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
                    connectToServer();
                    item.setTitle(R.string.settings_disconnect);
                } else {
                    disconnectFromServer();
                    item.setTitle(R.string.settings_connect);
                }
                return true;
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

        setupMqtt();
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

    private class ConnectTask extends AsyncTask<MqttClient, Void, Void> {

        private ProgressDialog dialog;
        private boolean isConnected = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Connecting. Please wait...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(MqttClient... params) {
            MqttClient client = params[0];

            if (client != null && !client.isConnected()) {
                try {
                    client.connect();
                    if (client.isConnected()) {
                        isConnected = true;
                        client.subscribe(TOPIC);
                        JSONObject json = new JSONObject();
                        json.put("status", 0);
                        mClient.publish(TOPIC, json.toString().getBytes(), 0, false);
                    }
                } catch (MqttException e) {
                    Log.d(TAG, "Could not connect to server...");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            invalidateOptionsMenu();
            updateActionbarSubtitle(isConnected);
            if (!isConnected) {
                Toast.makeText(MainActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (dialog != null) {
                dialog.dismiss();
            }
            invalidateOptionsMenu();
        }
    }

    private class DisconnectTask extends AsyncTask<MqttClient, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Disconnecting. Please wait...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(MqttClient... params) {
            MqttClient client = params[0];

            if (client != null) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            invalidateOptionsMenu();
            updateActionbarSubtitle(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (dialog != null) {
                dialog.dismiss();
            }
            invalidateOptionsMenu();
        }
    }
}

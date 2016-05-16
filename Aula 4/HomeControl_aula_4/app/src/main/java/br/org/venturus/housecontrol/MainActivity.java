package br.org.venturus.housecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mBrokerURL;
    private String mBrokerPort;

    // UI
    private TextView mTextTemp;
    private TextView mTempUpdate;
    private Switch mLightSwitch;
    private TextView mSwitchText;
    private ImageView mCardBackground;
    private ImageView mLightBulb;

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
                updateSwitchState(button.isChecked());
            }
        });

        mTextTemp = (TextView) findViewById(R.id.temperature);
        mTempUpdate = (TextView) findViewById(R.id.temp_update);
        mTextTemp.setText("-.-");
        mTempUpdate.setText("");

        updateBrokerInfo();
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
                } else {
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
}

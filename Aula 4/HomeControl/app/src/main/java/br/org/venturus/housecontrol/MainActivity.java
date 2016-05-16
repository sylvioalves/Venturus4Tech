package br.org.venturus.housecontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onSwitchClicked(View view) {
        Switch switchView = (Switch) view;

        TextView switchText = (TextView) findViewById(R.id.lamp_switch_text);
        ImageView cardBackground = (ImageView) findViewById(R.id.card_bg);
        ImageView lightBulb = (ImageView) findViewById(R.id.lamp);

        if (switchView.isChecked()) {
            switchText.setText(R.string.switch_text_on);

            cardBackground.setImageResource(R.drawable.bgon);

            lightBulb.setImageResource(R.drawable.icon_luz_on);
        } else {
            switchText.setText(R.string.switch_text_off);

            cardBackground.setImageResource(R.drawable.bgoff);

            lightBulb.setImageResource(R.drawable.icon_luz_off);
        }
    }
}

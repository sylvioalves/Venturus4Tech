package br.org.venturus.housecontrol;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsPreference extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Atualiza o valor do endereço do broker
        EditTextPreference pref_broker = (EditTextPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_key_server));
        if (pref_broker != null) {
            pref_broker.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((String) newValue);
                    return true;
                }
            });

            pref_broker.setSummary(pref_broker.getText());
        }

        // Atualiza o valor da porta do servidor
        EditTextPreference pref_port = (EditTextPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_key_port));
        if (pref_port != null) {
            pref_port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((String) newValue);
                    return true;
                }
            });

            pref_port.setSummary(pref_port.getText());
        }
    }
}

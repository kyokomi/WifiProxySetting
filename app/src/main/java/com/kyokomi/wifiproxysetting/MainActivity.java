package com.kyokomi.wifiproxysetting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        SharedPreferences mSharedPreferences;

        @InjectView(R.id.wifiListView)
        ListView mListView;

        @OnItemClick(R.id.wifiListView)
        public void showWifiConfigDetail(int position) {
            final String ssid = (String) mListView.getAdapter().getItem(position);

            final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);

            // WIFI 強制ON
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            // 選択したSSIDの設定を取得
            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            Optional<WifiConfiguration> wifiConfigurationOptional = Iterables.tryFind(wifiConfigurations, new Predicate<WifiConfiguration>() {
                @Override
                public boolean apply(WifiConfiguration input) {
                    return (input.SSID.equals(ssid));
                }
            });

            if (!wifiConfigurationOptional.isPresent()) {
                // 見つからなければおわり
                return;
            }

            // 永続化用にJSON文字列に変換
            final String wifiConfig = new Gson().toJson(wifiConfigurationOptional.get());
            Log.d(ssid, wifiConfig);

            // 設定確認のダイアログを表示
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.getActivity());
            // TODO: 固定文言
            alertBuilder.setTitle("Wifi設定確認");
            alertBuilder.setMessage(wifiConfig);
            alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mSharedPreferences.contains(ssid)) {
                        String settingJson = mSharedPreferences.getString(ssid, null);
                        Log.d("Preferences : " + ssid, settingJson);
                    } else {
                        mSharedPreferences.edit().putString(ssid, wifiConfig).apply();
                    }
                }
            });
            // Saveがある場合loadボタンを出す
            if (mSharedPreferences.contains(ssid)) {
                alertBuilder.setNeutralButton("Load", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String settingJson = mSharedPreferences.getString(ssid, null);
                        Log.d("Preferences : " + ssid, settingJson);
                        // 更新
                        WifiConfiguration wifiConfiguration = new Gson().fromJson(settingJson, WifiConfiguration.class);
                        wifiManager.updateNetwork(wifiConfiguration);
                    }
                });
            }
            alertBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // なんもしない
                }
            });
            // アラートダイアログのキャンセルが可能
            alertBuilder.setCancelable(true);
            AlertDialog alertDialog = alertBuilder.create();
            // アラートダイアログを表示
            alertDialog.show();
        }

        @InjectView(R.id.button1)
        Button mButton1;

        @OnClick(R.id.button1)
        public void showConfiguredNetworks() {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);

            // WIFI on
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);

            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                adapter.add(wifiConfiguration.SSID);
            }
            mListView.setAdapter(adapter);
        }

        @InjectView(R.id.button2)
        Button mButton2;

        @OnClick(R.id.button2)
        public void showScanResults() {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);

            // WIFI on
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);

            List<ScanResult> scanResultList = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResultList) {
                adapter.add(scanResult.SSID);
            }
            mListView.setAdapter(adapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ButterKnife.inject(this, rootView);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

            showConfiguredNetworks();

            return rootView;
        }
    }
}

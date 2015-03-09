package com.kyokomi.wifiproxysetting.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.kyokomi.wifiproxysetting.R;
import com.kyokomi.wifiproxysetting.WifiProxySettingApplication;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends BaseFragment {

    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    WifiManager wifiManager;
    @Inject
    Gson gson;

    @InjectView(R.id.wifiListView)
    ListView mListView;
    @InjectView(R.id.button1)
    Button mButton1;
    @InjectView(R.id.button2)
    Button mButton2;

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ((WifiProxySettingApplication) getActivity().getApplication()).component().inject(this);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        showConfiguredNetworks();

        return rootView;
    }

    @OnItemClick(R.id.wifiListView)
    public void showWifiConfigDetail(int position) {
        final String ssid = (String) mListView.getAdapter().getItem(position);

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
        final String wifiConfig = gson.toJson(wifiConfigurationOptional.get());
        Log.d(ssid, wifiConfig);

        // 設定確認のダイアログを表示
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.getActivity());
        // TODO: 固定文言
        alertBuilder.setTitle("Wifi設定確認");
        alertBuilder.setMessage(wifiConfig);
        alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sharedPreferences.contains(ssid)) {
                    String settingJson = sharedPreferences.getString(ssid, null);
                    Log.d("Preferences : " + ssid, settingJson);
                } else {
                    sharedPreferences.edit().putString(ssid, wifiConfig).apply();
                }
            }
        });
        // Saveがある場合loadボタンを出す
        if (sharedPreferences.contains(ssid)) {
            alertBuilder.setNeutralButton("Load", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String settingJson = sharedPreferences.getString(ssid, null);
                    Log.d("Preferences : " + ssid, settingJson);
                    // 更新
                    WifiConfiguration wifiConfiguration = gson.fromJson(settingJson,
                            WifiConfiguration.class);
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

    @OnClick(R.id.button1)
    public void showConfiguredNetworks() {
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

    @OnClick(R.id.button2)
    public void showScanResults() {
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
}
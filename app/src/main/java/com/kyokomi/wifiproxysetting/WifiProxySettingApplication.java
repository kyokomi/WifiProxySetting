/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kyokomi.wifiproxysetting;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import com.google.gson.Gson;
import com.kyokomi.wifiproxysetting.ui.activity.BaseActivity;
import com.kyokomi.wifiproxysetting.ui.fragment.BaseFragment;
import com.kyokomi.wifiproxysetting.ui.fragment.PlaceholderFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

public class WifiProxySettingApplication extends Application {
    @Singleton
    @Component(modules = AndroidModule.class)
    public interface ApplicationComponent {
        void inject(WifiProxySettingApplication application);

        void inject(BaseActivity baseActivity);

        void inject(com.kyokomi.wifiproxysetting.ui.activity.MainActivity mainActivity);
        void inject(BaseFragment baseFragment);
        void inject(PlaceholderFragment placeholderFragment);
    }

    @Inject
    SharedPreferences sharedPreferences; // for some reason.
    @Inject
    WifiManager wifiManager;
    @Inject
    Gson gson;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = Dagger_WifiProxySettingApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component().inject(this); // As of now, LocationManager should be injected into this.
    }

    public ApplicationComponent component() {
        return component;
    }
}

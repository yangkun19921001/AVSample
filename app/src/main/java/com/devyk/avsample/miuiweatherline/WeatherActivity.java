package com.devyk.avsample.miuiweatherline;

import com.devyk.avsample.R;
import com.devyk.ikavedit.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author  : devyk on 2020-06-13 10:59
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is WeatherActivity
 * </pre>
 */
public class WeatherActivity extends BaseActivity {
    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void init() {
        MiuiWeatherView   weatherView = (MiuiWeatherView) findViewById(R.id.weather);
        List<WeatherBean> data = new ArrayList<>();
        //add your WeatherBean to data
        WeatherBean b1 = new WeatherBean(WeatherBean.SUN,20,"05:00");
        WeatherBean b2 = new WeatherBean(WeatherBean.RAIN,30,"05:30");
        WeatherBean b3 = new WeatherBean(WeatherBean.RAIN,40,"07:30");
        WeatherBean b4 = new WeatherBean(WeatherBean.RAIN,10,"10:30");
        WeatherBean b5 = new WeatherBean(WeatherBean.RAIN,2,"12:00");
        WeatherBean b6 = new WeatherBean(WeatherBean.RAIN,40,"13:30");

        WeatherBean b7 = new WeatherBean(WeatherBean.SUN,30,"14:30");
        WeatherBean b8 = new WeatherBean(WeatherBean.SUN,20,"15:30");
        WeatherBean b9 = new WeatherBean(WeatherBean.RAIN,28,"17:30");
        WeatherBean b10 = new WeatherBean(WeatherBean.SUN,32,"18:30");
        WeatherBean b11 = new WeatherBean(WeatherBean.RAIN,10,"19:30");
        WeatherBean b12 = new WeatherBean(WeatherBean.SUN,18,"20:30");
        //b3、b4...bn
        data.add(b1);
        data.add(b2);
        data.add(b3);
        data.add(b4);
        data.add(b5);
        data.add(b6);
        data.add(b7);
        data.add(b8);
        data.add(b9);
        data.add(b10);
        data.add(b11);
        data.add(b12);
        weatherView.setData(data);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_weather;
    }
}

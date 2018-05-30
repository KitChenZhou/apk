package com.ckt.testauxiliarytool.autosleepwake;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.adapter.FragmentAdapter;

public class ASWMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asw_activity_auto_sleep_wake);
        initViews();
    }

    private void initViews() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tl_Tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.vp_fragment);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), getTabTitles());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private String[] getTabTitles() {
        return getResources().getStringArray(R.array.tabTitles);
    }
}

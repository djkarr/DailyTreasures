package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.danlew.android.joda.JodaTimeAndroid;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);

    }

    @OnClick(R.id.schedule_button)
    public void onScheduleClick(){
//        Toast.makeText(this, "Schedule Clicked!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SchedulerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.read_button)
    public void onReadClick(){
        Intent intent = new Intent(this, ReadingActivity.class);
        startActivity(intent);
    }
}

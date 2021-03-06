package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.read_button)
    Button mReadButton;

    @BindView(R.id.read_button_two)
    Button mReadButtonTwo;

    @BindView(R.id.emanna_button)
    Button mEmannaButton;

    @BindView(R.id.catch_up_button_one)
    Button mCatchUpButtonOne;

    @BindView(R.id.catch_up_button_two)
    Button mCatchUpButtonTwo;

    @BindView(R.id.schedule_button)
    Button mScheduleButtonOne;

    @BindView(R.id.schedule_button_two)
    Button mScheduleButtonTwo;

    private boolean mScheduleOneExists;
    private boolean mScheduleTwoExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        enableButtons();
        checkSchedulesExist();
    }

    void enableButtons(){
        mReadButton.setEnabled(true);
        mReadButtonTwo.setEnabled(true);
        mEmannaButton.setEnabled(true);
        mCatchUpButtonOne.setEnabled(true);
        mCatchUpButtonTwo.setEnabled(true);
        mScheduleButtonOne.setEnabled(true);
        mScheduleButtonTwo.setEnabled(true);
    }

    private void checkSchedulesExist(){
        File fileOne = new File(getApplicationContext().getFilesDir().getPath() + "/schedule");
        File fileTwo = new File(getApplicationContext().getFilesDir().getPath() + "/schedule2");
        mScheduleOneExists = fileOne.exists();
        mScheduleTwoExists = fileTwo.exists();
    }

    @OnClick(R.id.schedule_button)
    public void onScheduleClick(){
        mScheduleButtonOne.setEnabled(false);
        Intent intent = new Intent(this, SchedulerActivity.class);
        intent.putExtra("scheduleNum", 1);
        startActivity(intent);
    }

    @OnClick(R.id.schedule_button_two)
    public void onScheduleTwoClick(){
        mScheduleButtonTwo.setEnabled(false);
        Intent intent = new Intent(this, SchedulerActivity.class);
        intent.putExtra("scheduleNum", 2);
        startActivity(intent);
    }

    @OnClick(R.id.read_button)
    public void onReadClick(){
        if(mScheduleOneExists){
            mReadButton.setEnabled(false);
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("scheduleNum", 1);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Reading Plan #1 has not been created!",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.read_button_two)
    public void onReadTwoClick(){
        if(mScheduleTwoExists){
            mReadButtonTwo.setEnabled(false);
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("scheduleNum", 2);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Reading Plan #2 has not been created!",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.emanna_button)
    public void onEmannaClick(){
        mEmannaButton.setEnabled(false);
        Intent intent = new Intent(this, EMannaActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.catch_up_button_one)
    void onCatchUpOneClick(){
        if(mScheduleOneExists){
            mCatchUpButtonOne.setEnabled(false);
            Intent intent = new Intent(this, ProgessActivity.class);
            intent.putExtra("scheduleNum", 1);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Readling Plan #1 has not been created!",Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.catch_up_button_two)
    void onCatchUpTwoClick(){
        if(mScheduleTwoExists){
            mCatchUpButtonTwo.setEnabled(false);
            Intent intent = new Intent(this, ProgessActivity.class);
            intent.putExtra("scheduleNum", 2);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Reading Plan #2 has not been created!",Toast.LENGTH_SHORT).show();
        }

    }
}

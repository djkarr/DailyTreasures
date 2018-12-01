package xyz.danielkarr.dailytreasures;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EMannaActivity extends AppCompatActivity {

    private String mDateString;
    private final String URL = "https://minister.emanna.com/todaysemanna.cfm?readingdate=";
    private String mCompleteURL;

    private final static String TAG = "eManna";

    @BindView(R.id.emanna_textview)
    TextView mEMannaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emanna);
        ButterKnife.bind(this);

        Date today = new Date();
        mDateString = dateToString(today);
        mCompleteURL = URL + mDateString;
        getWebsite();
    }

    public String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if(day < 10){
            return month + "/0" + day + "/" + year;
        }
        return month + "/" + day + "/" + year;
    }

    private void getWebsite() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect(mCompleteURL).get();
                    Elements text = doc.select("p");
                    System.out.println(text.toString());


                    for (Element t : text) {
                        builder.append(t.text()).append("\n\n");
                        System.out.println(t);
                    }
                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEMannaView.setText(builder.toString());
                    }
                });
            }
        }).start();
    }

}
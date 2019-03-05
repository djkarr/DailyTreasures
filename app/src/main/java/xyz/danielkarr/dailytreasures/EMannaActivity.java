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
        String dateString = dateToString(today);
        String URL = "https://minister.emanna.com/todaysemanna.cfm?readingdate=";
        mCompleteURL = URL + dateString;
        getWebsite();
        mEMannaView.setTextIsSelectable(true);
    }

    /**
     * Return String representation of a date, MM/DD/YYYY, if month or day <10 adds a zero to it
     * to make it the same format as the eManna site
     * @param date to get a string of
     * @return String representation of date
     */
    public String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String formatted = String.format("%02d/%02d/" + year, month, day);
        return formatted;
    }

    private void getWebsite() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect(mCompleteURL).get();
                    Elements text = doc.select("p");

                    for (Element t : text) {
                        builder.append(t.text()).append("\n\n");
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

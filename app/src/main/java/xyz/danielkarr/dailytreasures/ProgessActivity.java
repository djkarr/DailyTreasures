package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class ProgessActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    private MyRecyclerViewAdapter adapter;
    private int mScheduleNum;
    private String mFileName;
    private ArrayList<String> mScheduleList;
    private ArrayList<String> mBookAbrList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        mScheduleNum = getIntent().getIntExtra("scheduleNum", 1);

        mBookAbrList = new ArrayList<>(Arrays.asList(
                "Gen","Exo","Lev","Num","Deu","Jsh","Jdg","Rut","1Sa","2Sa","1Ki","2Ki",
                "1Ch","2Ch","Ezr","Neh","Est","Job","Psa","Prv","Ecc","SoS","Isa","Jer",
                "Lam","Ezk","Dan","Hos","Joe","Amo","Oba","Jon","Mic","Nah","Hab","Zep",
                "Hag","Zec","Mal", "Mat","Mrk","Luk","Jhn","Act","Rom","1Co","2Co","Gal",
                "Eph","Phi","Col","1Th","2Th","1Ti","2Ti","Tit","Phm","Heb","Jam","1Pe",
                "2Pe","1Jo","2Jo","3Jo","Jud","Rev"
        ));

        if(mScheduleNum == 1){
            mFileName = "schedule";
        } else {
            mFileName = "schedule2";
        }

        // data to populate the RecyclerView with
        ArrayList<String> scheduleListRaw = new ArrayList<>();
        mScheduleList = new ArrayList<>();

        try{
            FileInputStream fis = this.openFileInput(mFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                scheduleListRaw.add(line);
            }
            bufferedReader.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        transformText(scheduleListRaw);

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvSchedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, mScheduleList, true);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        String book = adapter.getItem(position);
        String[] parts = book.split(" ");
        String date = parts[0];
        Intent intent=new Intent(this, ReadingActivity.class);
        intent.putExtra("dateString", date);
        intent.putExtra("scheduleNum", mScheduleNum);
        startActivity(intent);
        finish();
    }

    void transformText(ArrayList<String> rawList){
        for(int i=1; i<rawList.size(); i++){
            String[] splitLine = rawList.get(i).split(" ");
            String date = splitLine[0];
            int sBookIndex = Integer.parseInt(splitLine[1]);
            int eBookIndex = Integer.parseInt(splitLine[4]);
            String sBook = getAbbreviation(sBookIndex);
            String eBook = getAbbreviation(eBookIndex);
            String complete = date + " " + sBook + " " + splitLine[2] + ":" + splitLine[3] + " - " +
                    eBook + " " + splitLine[5] + ":" + splitLine[6];
            mScheduleList.add(complete);
        }
    }

    /**
     * Gets the book abbreviation to display in the textView.
     * @param booknum book index to retrieve, passed as 1 based.
     * @return the book abbreviation.
     */
    public String getAbbreviation(int booknum){
        return mBookAbrList.get(booknum-1);
    }
}

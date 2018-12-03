package xyz.danielkarr.dailytreasures;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadingActivity extends AppCompatActivity {
    private List<String> mLines;
    private ArrayList<String> mBookAbrList;
    private ArrayList<String> mCompleteVerseList;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDB;
    private String mStringDate;
    private int mTodayIndex;

    // Schedule Variables
    private int mSBook;
    private int mSChap;
    private int mSVerse;
    private int mEBook;
    private int mEChap;
    private int mEVerse;
    private boolean mIsSingleChap;
    private boolean mIsSingleBook;

    private int mScheduleNum;
    private String mFileName;

    Parcelable state;

    private static final String TAG = "READINGACTIVITY";

    static class BibleCols implements BaseColumns {
        static final String TABLE_NAME = "bible";
        static final String COL_BOOK = "book";
        static final String COL_CHAPTER = "chapter";
        static final String COL_VERSE = "verse";
        static final String COL_TEXT = "versetext";
    }

    @BindView(R.id.reading_scroll_view)
    ScrollView mScrollView;

    @BindView(R.id.reading_view)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        ButterKnife.bind(this);

        mScheduleNum = getIntent().getIntExtra("scheduleNum",1);
        if(mScheduleNum == 1){
            mFileName = "schedule";
            setTitle("Reading Portion " + 1);
        } else {
            mFileName = "schedule2";
            setTitle("Reading Portion " + 2);
        }
        mLines = new ArrayList<>();
        mCompleteVerseList = new ArrayList<>();

        try{
            FileInputStream fis = this.openFileInput(mFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                mLines.add(line);
            }
            bufferedReader.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        mTextView.setMovementMethod(new ScrollingMovementMethod());

        mDbHelper = DatabaseHelper.getInstance(this);
        mDB = mDbHelper.getReadableDatabase();
        Date today = new Date();
        mStringDate = dateToString(today);
        // With new summary line as first line, start at index 1
        for(int i=1; i<mLines.size(); i++){
            if(mLines.get(i).startsWith(mStringDate)){
                mTodayIndex = i;
                break;
            }
        }

        mBookAbrList = new ArrayList<>(Arrays.asList(
                "Gen","Exo","Lev","Num","Deu","Jsh","Jdg","Rut","1Sa","2Sa","1Ki","2Ki",
                "1Ch","2Ch","Ezr","Neh","Est","Job","Psa","Prv","Ecc","SoS","Isa","Jer",
                "Lam","Ezk","Dan","Hos","Joe","Amo","Oba","Jon","Mic","Nah","Hab","Zep",
                "Hag","Zec","Mal", "Mat","Mrk","Luk","Jhn","Act","Rom","1Co","2Co","Gal",
                "Eph","Phi","Col","1Th","2Th","1Ti","2Ti","Tit","Phm","Heb","Jam","1Pe",
                "2Pe","1Jo","2Jo","3Jo","Jud","Rev"
        ));

        setScheduleVariables();
        populateVerseList();
        populateTextView();
    }

    @Override
    public void onResume(){
        super.onResume();
        setScroll();
    }

    @Override
    public void onPause(){
        super.onPause();
        int scrollY = mScrollView.getScrollY();
        writeScrollYToFile(scrollY);
    }

    private void writeScrollYToFile(int y){
        String filename;
        if(mScheduleNum == 1){
            filename = "scrollOne";
        } else {
            filename = "scrollTwo";
        }

        FileOutputStream outputStream;
        String summary = mStringDate + " " + y;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(summary.getBytes());
            outputStream.close();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScroll(){
        String filename;
        if(mScheduleNum == 1){
            filename = "scrollOne";
        } else {
            filename = "scrollTwo";
        }
        File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + filename);
        if(file.exists()){
            try{
                FileInputStream fis = this.openFileInput(filename);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line = bufferedReader.readLine();
                bufferedReader.close();

                String[] parsedLine = line.split(" ");
                if(parsedLine[0].equals(mStringDate)){
                    final int yScroll = Integer.parseInt(parsedLine[1]);
                    //This works for state, but not working for my case
                    System.out.println(yScroll);
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.scrollTo(0, yScroll);
                        }
                    });
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void populateTextView(){
        StringBuilder builder = new StringBuilder();
        for (String verse : mCompleteVerseList) {
            builder.append(verse).append("\n");
        }

        mTextView.setText(builder.toString());
    }

    public String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    public String getAbbreviation(int booknum){
        return mBookAbrList.get(booknum-1);
    }

    private void setScheduleVariables() {
        String[] splitLine = mLines.get(mTodayIndex).split(" ");
        mSBook = Integer.parseInt(splitLine[1]);
        mSChap = Integer.parseInt(splitLine[2]);
        mSVerse = Integer.parseInt(splitLine[3]);
        mEBook = Integer.parseInt(splitLine[4]);
        mEChap = Integer.parseInt(splitLine[5]);
        mEVerse = Integer.parseInt(splitLine[6]);
        mIsSingleBook = mSBook == mEBook;
        mIsSingleChap = mIsSingleBook && mSChap == mEChap;
    }

    private String singleChapterQuery(){
        return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
    }

    private String singleBookQuery(){
        int spreadAcrossChapters = mEChap - mSChap + 1;
        // Two chapters, union with two queries
        if(spreadAcrossChapters < 3){
            return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
            " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                    " union " + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mEBook +
                    " and " + BibleCols.COL_CHAPTER + " = " + mEChap + " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
        } else {
            return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                    " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                    " union " + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                    " and " + BibleCols.COL_CHAPTER + " > " + mSChap + " and " + BibleCols.COL_CHAPTER + " < " + mEChap + " union "
                    + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mEBook +
                    " and " + BibleCols.COL_CHAPTER + " = " + mEChap + " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
        }
    }

    private String multipleBookQuery(){
        String q1 = "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                " union " + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " > " + mSChap + " union ";
        String q2 = "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " > " + mSBook + " and " +
                BibleCols.COL_BOOK + " < " + mEBook + " union ";
        String q3 = "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mEBook + " and " +
                BibleCols.COL_CHAPTER + " < " + mEChap + " union ";
        String q4 = "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mEBook + " and " +
                BibleCols.COL_CHAPTER + " = " + mEChap + " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
        return q1 + q2 + q3 + q4;
    }

    private void populateVerseList(){
        String selectionQuery;
        if(mIsSingleChap){
            selectionQuery = singleChapterQuery();
        } else if (mIsSingleBook){
            selectionQuery = singleBookQuery();
        } else {
            selectionQuery = multipleBookQuery();
        }

        Cursor c = mDB.rawQuery(selectionQuery, null);
        String chap, verse, text;
        int bookNum;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            bookNum = c.getInt(0);
            chap = c.getString(1);
            verse = c.getString(2);
            text = c.getString(3);
            String completeVerse = getAbbreviation(bookNum) + " " + chap + ":" + verse + " " + text;
            mCompleteVerseList.add(completeVerse);
            c.moveToNext();
        }
        c.close();
    }
}

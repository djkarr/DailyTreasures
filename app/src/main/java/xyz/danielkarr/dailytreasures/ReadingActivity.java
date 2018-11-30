package xyz.danielkarr.dailytreasures;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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

    private static final String TAG = "READINGACTIVITY";

    public static class BibleCols implements BaseColumns {
        public static final String TABLE_NAME = "bible";
        public static final String COL_BOOK = "book";
        public static final String COL_CHAPTER = "chapter";
        public static final String COL_VERSE = "verse";
        public static final String COL_TEXT = "versetext";
    }

    @BindView(R.id.reading_view)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        ButterKnife.bind(this);
        Log.i(TAG, "onCreate: FILELIST" + fileList().toString());
        String[] list = fileList();
        for (String s:list
             ) {
            Log.i(TAG, "onCreate: FILELIST " + s);
        }

        mLines = new ArrayList<>();
        mCompleteVerseList = new ArrayList<>();
        String curLine = null;

        try{
            FileInputStream fis = this.openFileInput("schedule");
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
        for(int i=0; i<mLines.size(); i++){
            Log.i(TAG, "onCreate: mLines " + mLines.get(i));
        }


//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(this.getFilesDir().getPath() +"/schedule"));
//            while ((curLine = reader.readLine()) != null) {
//                mLines.add(curLine);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        mTextView.setMovementMethod(new ScrollingMovementMethod());


        mDbHelper = new DatabaseHelper(this);
        mDB = mDbHelper.getReadableDatabase();
        Date today = new Date();
        mStringDate = dateToString(today);
        for(int i=0; i<mLines.size(); i++){
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


//        Log.i(TAG, "onCreate: TODAY DATE STRING " + mStringDate);
//        Log.i(TAG, "onCreate: TODAY INDEX " + mTodayIndex);
//        Log.i(TAG, "onCreate: TODAY ENTRY " + mLines.get(mTodayIndex));
//        Log.i(TAG, "onCreate: TodayIndex + TodayEntry " + mTodayIndex + " " + mLines.get(mTodayIndex));


    }

    private void populateTextView(){
        StringBuilder builder = new StringBuilder();
        for (String verse : mCompleteVerseList) {
            builder.append(verse + "\n");
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
        if(mSBook == mEBook){
            mIsSingleBook = true;
        } else {
            mIsSingleBook = false;
        }
        if(mIsSingleBook && mSChap == mEChap){
            mIsSingleChap = true;
        } else {
            mIsSingleChap = false;
        }
//        Log.i(TAG, "setScheduleVariables: AFTER PARSE " + mSBook + " " + mSChap + " " + mSVerse + " " + mEBook + " " +  mEChap
//                + " " + mEVerse);
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
        String selectionQuery = singleChapterQuery();
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
//            Log.i(TAG, "singleChapterQuery: COMPLETE VERSE: " + completeVerse);
            c.moveToNext();
        }
        c.close();
    }


}

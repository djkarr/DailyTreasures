package xyz.danielkarr.dailytreasures;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.PrecomputedText;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
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
import butterknife.OnClick;

public class ReadingActivity extends AppCompatActivity {
    // lines of schedule
    private List<String> mLines;
    private ArrayList<String> mBookAbrList;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDB;
    private String mStringDate;
    private int mTodayIndex;
    private String mTitle;

    // Schedule Variables, S for "starting" : E for "ending"
    private int mSBook;
    private int mSChap;
    private int mSVerse;
    private int mEBook;
    private int mEChap;
    private int mEVerse;
    private boolean mIsSingleChap;
    private boolean mIsSingleBook;
    // Wraps from Revelation --> Genesis
    private boolean mWrapsAround;

    private int mScheduleNum;
    private String mFileName;

    private float mSavedScrollValue;

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
        mBookAbrList = new ArrayList<>(Arrays.asList(
                "Gen","Exo","Lev","Num","Deu","Jsh","Jdg","Rut","1Sa","2Sa","1Ki","2Ki",
                "1Ch","2Ch","Ezr","Neh","Est","Job","Psa","Prv","Ecc","SoS","Isa","Jer",
                "Lam","Ezk","Dan","Hos","Joe","Amo","Oba","Jon","Mic","Nah","Hab","Zep",
                "Hag","Zec","Mal", "Mat","Mrk","Luk","Jhn","Act","Rom","1Co","2Co","Gal",
                "Eph","Phi","Col","1Th","2Th","1Ti","2Ti","Tit","Phm","Heb","Jam","1Pe",
                "2Pe","1Jo","2Jo","3Jo","Jud","Rev"
        ));

        if(savedInstanceState == null){
            // With new summary line as first line, start at index 1
            for(int i=1; i<mLines.size(); i++){
                if(mLines.get(i).startsWith(mStringDate)){
                    mTodayIndex = i;
                    break;
                }
            }
            if(mTodayIndex == 0){
                invalidSchedule();
            } else {
                setScheduleVariables();
                mTextView.setText(("Reading from database, please wait..."));
                populateVerseList();
            }

        } else {
            mTextView.setText(savedInstanceState.getCharSequence("verseText"));
            setScroll();
            mTitle = savedInstanceState.getString("title");
            setTitle(mTitle);
        }

    }

    /**
     * Alerts user that their schedule is no longer current and finishes the activity.
     */
    void invalidSchedule(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle("Completed Schedule").setMessage("This reading plan has either ended, or its start date is set for a future date. " +
                "You can go back and edit the reading plan to fix this.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }

    void populateTitle(){
        String sbook = getAbbreviation(mSBook);
        String ebook = getAbbreviation(mEBook);
        mTitle = "Portion #" + mScheduleNum + "   (" + sbook + " " + Integer.toString(mSChap) + ":" +
                Integer.toString(mSVerse) + " - " + ebook + " " + Integer.toString(mEChap) + ":" + Integer.toString(mEVerse) +
                ")";
        setTitle(mTitle);
    }


    @OnClick(R.id.save_place_button)
    void saveScrollPlace(){
        mSavedScrollValue = getScrollSpot();
    }

    @OnClick(R.id.restore_place_button)
    void restoreReadingPlace(){
        setScrollSpot(mSavedScrollValue);
    }

    @OnClick(R.id.jump_start_button)
    void jumpStart(){
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0,0);
            }
        });
    }

    @OnClick(R.id.jump_end_button)
    void jumpEnd(){
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(mScrollView.FOCUS_DOWN);
            }
        });
    }


    /**
     * Saves scroll position when the user leaves so that they can resume their reading.
     */
    @Override
    public void onPause(){
        super.onPause();
        mSavedScrollValue = getScrollSpot();
        writeScrollYToFile(mSavedScrollValue);
    }

    /**
     * Saves today's reading portion's text in the event of screen rotation.
     * @param outState Bundle to save.
     */
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        CharSequence verseText = mTextView.getText();
        outState.putCharSequence("verseText", verseText);
        outState.putString("title", mTitle);
    }

    /**
     * Writes the scroll position out to the appropriate file.
     * @param y scroll position y float value.
     */
    private void writeScrollYToFile(float y){
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the saved scroll position from the appropriate file and calls setScrollSpot
     * with that value.
     */
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
                    final float yScroll = Float.parseFloat(parsedLine[1]);
                    mSavedScrollValue = yScroll;
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            setScrollSpot(yScroll);
                        }
                    });
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the reader's current scroll position.
     * @return scroll position y value.
     */
    private float getScrollSpot() {
        int y = mScrollView.getScrollY();
        Layout layout = mTextView.getLayout();
        int topPadding = -layout.getTopPadding();
        if (y <= topPadding) {
            return (float) (topPadding - y) / mTextView.getLineHeight();
        }

        int line = layout.getLineForVertical(y - 1) + 1;
        int offset = layout.getLineStart(line);
        int above = layout.getLineTop(line) - y;
        return offset + (float) above / mTextView.getLineHeight();
    }

    /**
     * Calculates scroll position and calls scrollTo on the scrollview.
     * @param spot the y value to scroll to.
     */
    private void setScrollSpot(float spot) {
        int offset = (int) spot;
        int above = (int) ((spot - offset) * mTextView.getLineHeight());
        Layout layout = mTextView.getLayout();
        int line = layout.getLineForOffset(offset);
        int y = (line == 0 ? -layout.getTopPadding() : layout.getLineTop(line))
                - above;
        mScrollView.scrollTo(0, y);
    }

    /**
     * Turns a date object into a useful string format.
     * @param date to be converted into string.
     * @return string representation of the date.
     */
    public String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    /**
     * Gets the book abbreviation to display in the textView.
     * @param booknum book index to retrieve, passed as 1 based.
     * @return the book abbreviation.
     */
    public String getAbbreviation(int booknum){
        return mBookAbrList.get(booknum-1);
    }

    /**
     * Parses out today's schedule line and assigns them to the proper variables used for querying DB.
     */
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
        mWrapsAround = mSBook > mEBook;
        populateTitle();
    }

    /**
     * Creates SQL query for use in the case that today's reading portion is all from the same chapter.
     * @return a string of the query.
     */
    private String singleChapterQuery(){
        return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
    }

    /**
     * Creates Sql query for use in the case that today's reading portion is all from the same book, but different chapters.
     * @return a string of the query.
     */
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

    /**
     * Creates sql query to retrieve a single, complete book.
     * @param bookIndex index of the book to query.
     * @return a string of the query.
     */
    private String wholeBookQuery(int bookIndex){
        return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + bookIndex;
    }

    /**
     * Creates sql query to retrieve the remainder of a book.
     * @return a string of the query.
     */
    private String remainderOfBookQuery(){
        return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " = " + mSChap + " and " + BibleCols.COL_VERSE + " >= " + mSVerse +
                " union " + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mSBook +
                " and " + BibleCols.COL_CHAPTER + " > " + mSChap;
    }

    /**
     * Creates sql query to retrieve the contents of the day's ending book.
     * @return a string of the query.
     */
    private String endBookQuery(){

        return "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK + " = " + mEBook + " and " +
                BibleCols.COL_CHAPTER + " < " + mEChap + " union " + "select * from " + BibleCols.TABLE_NAME + " where " + BibleCols.COL_BOOK +
                " = " + mEBook + " and " + BibleCols.COL_CHAPTER + " = " + mEChap + " and " + BibleCols.COL_VERSE + " <= " + mEVerse;
    }

    /**
     * Builds query string then calls Query on DB.
     */
    private void populateVerseList(){
        String selectionQuery = "";
        if(mIsSingleChap){
            selectionQuery = singleChapterQuery();
        } else if (mIsSingleBook){
            selectionQuery = singleBookQuery();
        } else {
            boolean finished = false;
            for(int i=mSBook; !finished; i = (i+1) % 66){
                int x = i;
                if(x == 0){ x = 66;}
                if(x == mSBook){
                    selectionQuery = remainderOfBookQuery() + " union ";
                } else if(x == mEBook){
                    selectionQuery += endBookQuery();
                    finished = true;
                } else {
                    selectionQuery += wholeBookQuery(x) + " union ";
                }
            }
        }
        callBackgroundQuery(selectionQuery);
    }

    /**
     * Calls background query on DB, uses different class if SDK >= 28.
     * @param query the DB query
     */
    void callBackgroundQuery(String query){
        if (android.os.Build.VERSION.SDK_INT >= 28){
            backgroundQueryAPI28 BQ = new backgroundQueryAPI28(this);
            BQ.execute(query);
        } else {
            backgroundQuery BQ = new backgroundQuery(this);
            BQ.execute(query);
        }
    }

    /**
     * AsyncTask class used to query DB in background. For use with SDK >= 28, uses PrecomputedText for setText.
     */
    private class backgroundQueryAPI28 extends AsyncTask<String, Void, PrecomputedText>{

        private final ReadingActivity mReadingActivity;
        final PrecomputedText.Params params = mTextView.getTextMetricsParams();

        backgroundQueryAPI28(ReadingActivity ra){
            mReadingActivity = ra;
        }

        @Override
        protected PrecomputedText doInBackground(String... query) {
            SQLiteDatabase db = mDbHelper.getInstance(getApplicationContext()).getReadableDatabase();
            Cursor c = db.rawQuery(query[0],null);
            StringBuilder builder = new StringBuilder();
            StringBuilder wrapBuilder = new StringBuilder();
            String chap, verse, text;
            int bookNum;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                bookNum = c.getInt(0);
                chap = c.getString(1);
                verse = c.getString(2);
                text = c.getString(3);
                String completeVerse = getAbbreviation(bookNum) + " " + chap + ":" + verse + " " + text;
                if(mWrapsAround && bookNum < mSBook){
                    wrapBuilder.append(completeVerse).append("\n");
                } else {
                    builder.append(completeVerse).append("\n");
                }
                c.moveToNext();
            }
            c.close();

            String allText;
            if(mWrapsAround){
                allText = builder.toString() + wrapBuilder.toString();
            } else {
                allText = builder.toString();
            }

            return PrecomputedText.create(allText, params);
        }

        @Override
        protected void onPostExecute(PrecomputedText result) {
            super.onPostExecute(result);
            mTextView.setText(result);
            mReadingActivity.setScroll();
        }
    }

    /**
     * AsyncTask class used to query DB in background. For use with SDK < 28, uses String for setText.
     */
    private class backgroundQuery extends AsyncTask<String, Void, String>{
        private final ReadingActivity mReadingActivity;

        backgroundQuery(ReadingActivity ra){
            mReadingActivity = ra;
        }

        @Override
        protected String doInBackground(String... query) {
            SQLiteDatabase db = mDbHelper.getInstance(getApplicationContext()).getReadableDatabase();
            Cursor c = db.rawQuery(query[0],null);
            StringBuilder builder = new StringBuilder();
            StringBuilder wrapBuilder = new StringBuilder();
            String chap, verse, text;
            int bookNum;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                bookNum = c.getInt(0);
                chap = c.getString(1);
                verse = c.getString(2);
                text = c.getString(3);
                String completeVerse = getAbbreviation(bookNum) + " " + chap + ":" + verse + " " + text;
                if(mWrapsAround && bookNum < mSBook){
                    wrapBuilder.append(completeVerse).append("\n");
                } else {
                    builder.append(completeVerse).append("\n");
                }
                c.moveToNext();
            }
            c.close();

            String allText;
            if(mWrapsAround){
                allText = builder.toString() + wrapBuilder.toString();
            } else {
                allText = builder.toString();
            }
            return allText;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mTextView.setText(result);
            mReadingActivity.setScroll();
        }
    }
}

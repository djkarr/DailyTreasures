package xyz.danielkarr.dailytreasures;

import org.joda.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to make passing data simpler for scheduler.
 */
class DailyPortion {

    private LocalDate mDate;
    // 1 based
    private int mStartingBookIndex;
    private int mStartingChapter;
    private int mStartingVerse;
    // 1 based
    private int mEndingBookIndex;
    private int mEndingChapter;
    private int mEndingVerse;

    DailyPortion(LocalDate date, int sBook, int sChap, int sVerse){
        mDate = date;
        mStartingBookIndex = sBook;
        mStartingChapter = sChap;
        mStartingVerse = sVerse;
    }

    public DailyPortion(LocalDate date, int sBook, int sChap, int sVerse, int eBook, int eChap, int eVerse){
        mDate = date;
        mStartingBookIndex = sBook;
        mStartingChapter = sChap;
        mStartingVerse = sVerse;
        mEndingBookIndex = eBook;
        mEndingChapter = eChap;
        mEndingVerse = eVerse;
    }

    public String toString(){
        Date d = mDate.toDate();
        String dateString = dateToString(d);
        return dateString + " " + mStartingBookIndex + " " + mStartingChapter + " " + mStartingVerse + " " +
                mEndingBookIndex + " " + mEndingChapter + " " + mEndingVerse;
    }

    private String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate date) {
        mDate = date;
    }

    int getStartingBookIndex() {
        return mStartingBookIndex;
    }

    public void setStartingBookIndex(int startingBookIndex) {
        mStartingBookIndex = startingBookIndex;
    }

    int getStartingChapter() {
        return mStartingChapter;
    }

    public void setStartingChapter(int startingChapter) {
        mStartingChapter = startingChapter;
    }

    int getStartingVerse() {
        return mStartingVerse;
    }

    public void setStartingVerse(int startingVerse) {
        mStartingVerse = startingVerse;
    }

    int getEndingBookIndex() {
        return mEndingBookIndex;
    }

    void setEndingBookIndex(int endingBookIndex) {
        mEndingBookIndex = endingBookIndex;
    }

    int getEndingChapter() {
        return mEndingChapter;
    }

    void setEndingChapter(int endingChapter) {
        mEndingChapter = endingChapter;
    }

    int getEndingVerse() {
        return mEndingVerse;
    }

    void setEndingVerse(int endingVerse) {
        mEndingVerse = endingVerse;
    }
}

package xyz.danielkarr.dailytreasures;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private DateListener mCallBack;
    private int year;
    private int month;
    private int day;

    public enum FragmentType{START_DATE, END_DATE}
    private FragmentType mFragmentType;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use the current date as the default date in the date picker
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);


        return new DatePickerDialog(getContext(), this, year, month, day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(getActivity() instanceof DateListener)) {
            throw new ClassCastException("The activity inflating this fragment must implement DateListener!");
        }
        mCallBack = (DateListener) getActivity();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        FragmentType fType = getFragmentType();
        mCallBack.onDatePicked(month,dayOfMonth,year, fType);
    }

    public interface DateListener {
        void onDatePicked(int month, int day, int year, FragmentType fType);
    }

    public void setFragmentType(FragmentType type){
        this.mFragmentType = type;
    }

    public FragmentType getFragmentType(){
        return mFragmentType;
    }

}
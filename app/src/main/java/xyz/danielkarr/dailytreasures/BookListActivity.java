package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter adapter;
    public enum FragmentType{START_BOOK, END_BOOK}
    private FragmentType mFragmentType;
    private int requestcode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // data to populate the RecyclerView with
        ArrayList<String> booklist = (ArrayList<String>) getIntent().getSerializableExtra("booklist");
        requestcode = getIntent().getIntExtra("requestcode",0);

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, booklist);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        String book = adapter.getItem(position);
        Intent intent=new Intent();
        intent.putExtra("ReturnedBook",book);
        setResult(requestcode,intent);
        finish();
    }

    public FragmentType getFragmentType() {
        return mFragmentType;
    }

    public void setFragmentType(FragmentType fragmentType) {
        mFragmentType = fragmentType;
    }
}

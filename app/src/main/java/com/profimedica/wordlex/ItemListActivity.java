package com.profimedica.wordlex;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {


    public void SaveLex(View view) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/data");
        if(!myDir.exists()) {
            boolean created = myDir.mkdirs();
            created = ! created;
        }

        String fname = "DeEn.txt";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            for (Word word: words)
            {
                out.write((word.Native + " = " + word.Foreign + " = " + String.valueOf(word.Bad) + " = " + String.valueOf(word.Bad) + " = " + String.valueOf(word.TimeSpend) + " = " + String.valueOf(word.FGood) + " = " + String.valueOf(word.FBad) + " = " + String.valueOf(word.FSpend) + "\n").getBytes());
            }
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendLex(View view)
    {
        File file = new File(Environment.getExternalStorageDirectory()
                .toString() + File.separator + "data" + File.separator + "DeEn.txt");
        File[] attachmentFiles = new File[1];
        attachmentFiles[0] = file;
        ArrayList<Uri> uriList = new ArrayList<Uri>();
        for (int i = 0; i < attachmentFiles.length; i++) {
            uriList.add(Uri.fromFile(attachmentFiles[i]));
        }


        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"start_florin@yahoo.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "This is sent from Android");
        // the attachment
        emailIntent .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        /** or use **/
        //emailIntent.setType("message/rfc822");
        startActivity(emailIntent);
    }
    List<Word> words;

    RecyclerView recyclerView;
    private void DisplaySortedBy(int parameter)
    {
        words = ReadSQL(parameter);

        //Collections.sort(words);
        //Collections.reverse(words);
        /*
        bNativeView.setBackgroundColor(parameter == 1 ? Color.BLUE : Color.GRAY);
        bForeignView.setBackgroundColor(parameter == 2 ? Color.BLUE : Color.GRAY);
        bGoodView.setBackgroundColor(parameter == 3 ? Color.BLUE : Color.GRAY);
        bBadView.setBackgroundColor(parameter == 4 ? Color.BLUE : Color.GRAY);
        bSpentView.setBackgroundColor(parameter == 5 ? Color.BLUE : Color.GRAY);
        bFGoodView.setBackgroundColor(parameter == 6 ? Color.BLUE : Color.GRAY);
        bFBadView.setBackgroundColor(parameter == 7 ? Color.BLUE : Color.GRAY);
        bFSpentView.setBackgroundColor(parameter == 8 ? Color.BLUE : Color.GRAY);

        TableLayout statisticsTable = (TableLayout) findViewById(R.id.StatisticsTable);

        for (int position = 0; position <words.size(); position++) {

            TableRow row= new TableRow(this);

            mNativeView = new TextView(this);
            mNativeView.setTextColor(Color.GRAY);
            mGoodView = new TextView(this);
            mGoodView.setTextColor(Color.GREEN);
            mBadView = new TextView(this);
            mBadView.setTextColor(Color.RED);
            mSpentView = new TextView(this);
            mSpentView.setTextColor(Color.BLUE);
            mForeignView = new TextView(this);
            mForeignView.setTextColor(Color.LTGRAY);
            mFGoodView = new TextView(this);
            mFGoodView.setTextColor(Color.GREEN);
            mFBadView = new TextView(this);
            mFBadView.setTextColor(Color.RED);
            mFSpentView = new TextView(this);
            mFSpentView.setTextColor(Color.GRAY);

            mNativeView.setText(words.get(position).Native);
            mGoodView.setText(String.valueOf(words.get(position).Good));
            mBadView.setText(String.valueOf(words.get(position).Bad));
            mSpentView.setText(String.valueOf(words.get(position).TimeSpend));
            mForeignView.setText(words.get(position).Foreign);
            mFGoodView.setText(String.valueOf(words.get(position).FGood));
            mFBadView.setText(String.valueOf(words.get(position).FBad));
            mFSpentView.setText(String.valueOf(words.get(position).FSpend));

            row.addView(mSpentView);
            row.addView(mGoodView);
            row.addView(mBadView);
            row.addView(mNativeView);
            row.addView(mForeignView);
            row.addView(mFGoodView);
            row.addView(mFBadView);
            row.addView(mFSpentView);
            statisticsTable.addView(row, position);

        }
        */
        //recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(words));
    }

    public void SortBy(View view) {
        //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.item_list);
        //assert recyclerView != null;
        int parameter = Integer.valueOf((String) view.getTag());
        DisplaySortedBy(parameter);
    }

    boolean ascendingOrder = true;

    public List<Word> ReadSQL(int criteria) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "SELECT " +
                WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                " FROM " + WordReaderContract.WordEntry.TABLE_NAME +
                " WHERE " + WordReaderContract.WordEntry.COLUMN_NAME_BAD +
                " >= " + 0 + " ORDER BY ";
        switch(criteria)
        {
            case 1:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_NATIVE;
                break;
            case 2:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN;
                break;
            case 3:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_GOOD;
                break;
            case 4:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_BAD;
                break;
            case 5:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_SPENT;
                break;
            case 6:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FGOOD;
                break;
            case 7:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FBAD;
                break;
            case 8:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FSPENT;
                break;
        }
        SQL += ascendingOrder ? " DESC" : " ASC";
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        ArrayList<Word> wordsToBeDiscovered = new ArrayList<Word>();
        while (cursor.isAfterLast() == false) {
            Word word = new Word(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getLong(7),
                    cursor.getLong(8),
                    cursor.getString(9)
            );
            wordsToBeDiscovered.add(word);
            cursor.moveToNext();
        }
        return wordsToBeDiscovered;
    }


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        recyclerView = (RecyclerView)findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        SaveLex(null);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        words = ReadSQL(5);
        //Collections.sort(words);
        Collections.reverse(words);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(words));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Word> mValues;

        public SimpleItemRecyclerViewAdapter(List<Word> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mNativeView.setText(mValues.get(position).Native);
            holder.mGoodView.setText(String.valueOf(mValues.get(position).Good));
            holder.mBadView.setText(String.valueOf(mValues.get(position).Bad));
            holder.mSpentView.setText(String.valueOf(mValues.get(position).TimeSpend));
            holder.mForeignView.setText(mValues.get(position).Foreign);
            holder.mFGoodView.setText(String.valueOf(mValues.get(position).FGood));
            holder.mFBadView.setText(String.valueOf(mValues.get(position).FBad));
            holder.mFSpentView.setText(String.valueOf(mValues.get(position).FSpend));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.Id);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.Id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNativeView;
            public final TextView mGoodView;
            public final TextView mBadView;
            public final TextView mSpentView;
            public final TextView mForeignView;
            public final TextView mFGoodView;
            public final TextView mFBadView;
            public final TextView mFSpentView;
            public Word mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mNativeView = (TextView) view.findViewById(R.id.Native);
                mGoodView = (TextView) view.findViewById(R.id.Good);
                mBadView = (TextView) view.findViewById(R.id.Bad);
                mSpentView = (TextView) view.findViewById(R.id.Spent);
                mForeignView = (TextView) view.findViewById(R.id.Foreign);
                mFGoodView = (TextView) view.findViewById(R.id.FGood);
                mFBadView = (TextView) view.findViewById(R.id.FBad);
                mFSpentView = (TextView) view.findViewById(R.id.FSpent);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNativeView.getText() + "'";
            }
        }
    }

    public void GoBackToQuiz(View view)
    {
        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);
    }
}

/*
package com.profimedica.wordlex;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private boolean mTwoPane;
    boolean ascendingOrder = true;
    public enum Criteria {
        cNative, cForeign, cGood, cBad, cSpent, cFGood, cFBad, cFSpent, cDictionary
    }
    Criteria criteria = Criteria.cSpent;


    public Button bNativeView;
    public Button bGoodView;
    public Button bBadView;
    public Button bSpentView;
    public Button bForeignView;
    public Button bFGoodView;
    public Button bFBadView;
    public Button bFSpentView;

    public TextView mNativeView;
    public TextView mGoodView;
    public TextView mBadView;
    public TextView mSpentView;
    public TextView mForeignView;
    public TextView mFGoodView;
    public TextView mFBadView;
    public TextView mFSpentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }


        bNativeView = (Button) findViewById(R.id.bNative);
        bGoodView = (Button) findViewById(R.id.bGood);
        bBadView = (Button) findViewById(R.id.bBad);
        bSpentView = (Button) findViewById(R.id.bSpent);
        bForeignView = (Button) findViewById(R.id.bForeign);
        bFGoodView = (Button) findViewById(R.id.bFGood);
        bFBadView = (Button) findViewById(R.id.bFBad);
        bFSpentView = (Button) findViewById(R.id.bFSpent);

        mNativeView = (TextView) findViewById(R.id.Native);
        mGoodView = (TextView) findViewById(R.id.Good);
        mBadView = (TextView) findViewById(R.id.Bad);
        mSpentView = (TextView) findViewById(R.id.Spent);
        mForeignView = (TextView) findViewById(R.id.Foreign);
        mFGoodView = (TextView) findViewById(R.id.FGood);
        mFBadView = (TextView) findViewById(R.id.FBad);
        mFSpentView = (TextView) findViewById(R.id.FSpent);

        //DisplaySortedBy(5);
        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        int parameter = 4;
        DisplaySortedBy(parameter);
        setupRecyclerView((RecyclerView) recyclerView);
//SortBy(ItemListActivity.this);
    }

    public List<Word> ReadSQL(int criteria) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "SELECT " +
                WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                " FROM " + WordReaderContract.WordEntry.TABLE_NAME +
                " WHERE " + WordReaderContract.WordEntry.COLUMN_NAME_BAD +
                " >= " + 0 + " ORDER BY ";
        switch(criteria)
        {
            case 1:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_NATIVE;
                break;
            case 2:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN;
                break;
            case 3:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_GOOD;
                break;
            case 4:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_BAD;
                break;
            case 5:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_SPENT;
                break;
            case 6:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FGOOD;
                break;
            case 7:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FBAD;
                break;
            case 8:
                SQL += WordReaderContract.WordEntry.COLUMN_NAME_FSPENT;
                break;
        }
        SQL += ascendingOrder ? " DESC" : " ASC";
        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToFirst();
        ArrayList<Word> wordsToBeDiscovered = new ArrayList<Word>();
        while (cursor.isAfterLast() == false) {
            Word word = new Word(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getLong(7),
                    cursor.getLong(8),
                    cursor.getString(9)
            );
            wordsToBeDiscovered.add(word);
            cursor.moveToNext();
        }
        return wordsToBeDiscovered;
    }

    public void SortBy(View view) {
        //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.item_list);
        //assert recyclerView != null;
        int parameter = Integer.valueOf((String) view.getTag());
        DisplaySortedBy(parameter);
    }

    private void DisplaySortedBy(int parameter)
    {
        List<Word> words = ReadSQL(parameter);

        //Collections.sort(words);
        //Collections.reverse(words);
        bNativeView.setBackgroundColor(parameter == 1 ? Color.BLUE : Color.GRAY);
        bForeignView.setBackgroundColor(parameter == 2 ? Color.BLUE : Color.GRAY);
        bGoodView.setBackgroundColor(parameter == 3 ? Color.BLUE : Color.GRAY);
        bBadView.setBackgroundColor(parameter == 4 ? Color.BLUE : Color.GRAY);
        bSpentView.setBackgroundColor(parameter == 5 ? Color.BLUE : Color.GRAY);
        bFGoodView.setBackgroundColor(parameter == 6 ? Color.BLUE : Color.GRAY);
        bFBadView.setBackgroundColor(parameter == 7 ? Color.BLUE : Color.GRAY);
        bFSpentView.setBackgroundColor(parameter == 8 ? Color.BLUE : Color.GRAY);

        TableLayout statisticsTable = (TableLayout) findViewById(R.id.StatisticsTable);

        for (int position = 0; position <words.size(); position++) {

            TableRow row= new TableRow(this);

            mNativeView = new TextView(this);
            mNativeView.setTextColor(Color.GRAY);
            mGoodView = new TextView(this);
            mGoodView.setTextColor(Color.GREEN);
            mBadView = new TextView(this);
            mBadView.setTextColor(Color.RED);
            mSpentView = new TextView(this);
            mSpentView.setTextColor(Color.BLUE);
            mForeignView = new TextView(this);
            mForeignView.setTextColor(Color.LTGRAY);
            mFGoodView = new TextView(this);
            mFGoodView.setTextColor(Color.GREEN);
            mFBadView = new TextView(this);
            mFBadView.setTextColor(Color.RED);
            mFSpentView = new TextView(this);
            mFSpentView.setTextColor(Color.GRAY);

            mNativeView.setText(words.get(position).Native);
            mGoodView.setText(String.valueOf(words.get(position).Good));
            mBadView.setText(String.valueOf(words.get(position).Bad));
            mSpentView.setText(String.valueOf(words.get(position).TimeSpend));
            mForeignView.setText(words.get(position).Foreign);
            mFGoodView.setText(String.valueOf(words.get(position).FGood));
            mFBadView.setText(String.valueOf(words.get(position).FBad));
            mFSpentView.setText(String.valueOf(words.get(position).FSpend));

            row.addView(mSpentView);
            row.addView(mGoodView);
            row.addView(mBadView);
            row.addView(mNativeView);
            row.addView(mForeignView);
            row.addView(mFGoodView);
            row.addView(mFBadView);
            row.addView(mFSpentView);
            statisticsTable.addView(row, position);

        }

        //recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(words));
     }

    //public void

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<Word> words = ReadSQL(5);
        //Collections.sort(words);
        //Collections.reverse(words);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(words));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Word> mValues;

        public SimpleItemRecyclerViewAdapter(List<Word> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mNativeView.setText(mValues.get(position).Native);
            holder.mGoodView.setText(String.valueOf(mValues.get(position).Good));
            holder.mBadView.setText(String.valueOf(mValues.get(position).Bad));
            holder.mSpentView.setText(String.valueOf(mValues.get(position).TimeSpend));
            holder.mForeignView.setText(mValues.get(position).Foreign);
            holder.mFGoodView.setText(String.valueOf(mValues.get(position).FGood));
            holder.mFBadView.setText(String.valueOf(mValues.get(position).FBad));
            holder.mFSpentView.setText(String.valueOf(mValues.get(position).FSpend));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.Id);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.Id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNativeView;
            public final TextView mGoodView;
            public final TextView mBadView;
            public final TextView mSpentView;
            public final TextView mForeignView;
            public final TextView mFGoodView;
            public final TextView mFBadView;
            public final TextView mFSpentView;
            public Word mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mNativeView = (TextView) view.findViewById(R.id.Native);
                mGoodView = (TextView) view.findViewById(R.id.Good);
                mBadView = (TextView) view.findViewById(R.id.Bad);
                mSpentView = (TextView) view.findViewById(R.id.Spent);
                mForeignView = (TextView) view.findViewById(R.id.Foreign);
                mFGoodView = (TextView) view.findViewById(R.id.FGood);
                mFBadView = (TextView) view.findViewById(R.id.FBad);
                mFSpentView = (TextView) view.findViewById(R.id.FSpent);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNativeView.getText() + "'";
            }
        }
    }

    public void GoBackToQuiz(View view)
    {
        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);
    }
}
*/
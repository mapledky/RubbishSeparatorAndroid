package com.maple.rubbishseparator.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.HistoryAdapter;
import com.maple.rubbishseparator.helper.DatabaseDAO;
import com.maple.rubbishseparator.util.ViewControl;

import java.util.List;

public class SearchActivity extends PermissionActivity implements View.OnClickListener , HistoryAdapter.ChooseHistoryListener {

    private EditText et_search;

    //data
    private List<String> history_data;
    private HistoryAdapter adapter;
    private ListView lv_history;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        init();
    }


    private void init() {
        //view
        et_search = findViewById(R.id.search_input);
        lv_history = findViewById(R.id.search_history);
        TextView tv_delete = findViewById(R.id.search_activity_delete);


        ImageView iv_elec = findViewById(R.id.search_activity_electronic);
        ImageView iv_shoes = findViewById(R.id.search_activity_shoes);
        ImageView iv_clothes = findViewById(R.id.search_activity_clothes);
        ImageView iv_books = findViewById(R.id.search_activity_books);
        ImageView iv_makeup = findViewById(R.id.search_activity_makeup);
        ImageView iv_pack = findViewById(R.id.search_activity_gift);
        ImageView iv_foot = findViewById(R.id.search_activity_sports);
        ImageView iv_instrument = findViewById(R.id.search_activity_instrument);


        iv_elec.setOnClickListener(this);
        iv_shoes.setOnClickListener(this);
        iv_clothes.setOnClickListener(this);
        iv_books.setOnClickListener(this);
        iv_makeup.setOnClickListener(this);
        iv_pack.setOnClickListener(this);
        iv_foot.setOnClickListener(this);
        iv_instrument.setOnClickListener(this);

        tv_delete.setOnClickListener(this);
        getHistory();
        attachListener();
    }

    //添加监听器
    private void attachListener() {
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!String.valueOf(et_search.getText()).equals("")) {
                    //开始搜索
                    toSearch(et_search.getText().toString());
                    //将搜索记录存入数据库
                    DatabaseDAO.getInstance(SearchActivity.this).updateHistory(et_search.getText().toString());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        });
    }

    //获取历史记录
    private void getHistory() {
        history_data = DatabaseDAO.getInstance(this).searchHistory();
        adapter = new HistoryAdapter(history_data, this);
        adapter.setHistoryListener(this);
        lv_history.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.search_activity_delete:
                    DatabaseDAO.getInstance(SearchActivity.this).deleteHistory();
                    history_data.clear();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.search_activity_electronic:
                    toSearch("电子产品");
                    break;
                case R.id.search_activity_shoes:
                    toSearch("鞋");
                    break;
                case R.id.search_activity_clothes:
                    toSearch("衣服");
                    break;
                case R.id.search_activity_books:
                    toSearch("书本");
                    break;
                case R.id.search_activity_makeup:
                    toSearch("化妆品");
                    break;
                case R.id.search_activity_gift:
                    toSearch("包装盒");
                    break;
                case R.id.search_activity_sports:
                    toSearch("足球");
                    break;
                case R.id.search_activity_instrument:
                    toSearch("吉他");
                    break;
                default:
                    break;
            }
        }
    }

    private void toSearch(String target) {
        Intent intent = new Intent(this, ShowResult.class);
        intent.putExtra("search_target", target);
        startActivity(intent);
    }

    @Override
    public void chooseHistory(String history) {
        toSearch(history);
    }
}
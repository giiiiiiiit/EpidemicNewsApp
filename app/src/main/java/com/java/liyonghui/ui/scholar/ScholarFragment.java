package com.java.liyonghui.ui.scholar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.material.tabs.TabLayout;
import com.java.liyonghui.R;
import com.java.liyonghui.ui.news.NewsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ScholarFragment extends Fragment {

    private ScholarViewModel scholarViewModel;
    private RecyclerView recyclerView;
    private ArrayList<Scholar> highlightScholarList = new ArrayList<>();
    private ArrayList<Scholar> passedAwayScholarList = new ArrayList<>();
    private TabLayout mTabLayout;
    private ScholarAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scholarViewModel =
                ViewModelProviders.of(this).get(ScholarViewModel.class);
        View root = inflater.inflate(R.layout.fragment_scholar, container, false);
        recyclerView = root.findViewById(R.id.scholar_view);

        mTabLayout = root.findViewById(R.id.scholarTabLayout);
        // 添加 tab item

        mTabLayout.addTab(mTabLayout.newTab().setText("高关注学者"));
        mTabLayout.addTab(mTabLayout.newTab().setText("追忆学者"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("高关注学者")) {
                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            mAdapter.setData(highlightScholarList);
                        }
                    });
                }
                if (tab.getText().equals("追忆学者")) {
                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            mAdapter.setData(passedAwayScholarList);
                        }
                    });
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client= new OkHttpClient();
                    Request.Builder reqBuild = new Request.Builder();
                    HttpUrl.Builder urlBuilder =HttpUrl.parse("https://innovaapi.aminer.cn/predictor/api/v1/valhalla/highlight/get_ncov_expers_list?v=2")
                            .newBuilder();
                    reqBuild.url(urlBuilder.build());
                    Request request = reqBuild.build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    JSONObject outerJSON = new JSONObject(responseData);
                    JSONArray jsonArray = outerJSON.getJSONArray("data");

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String avatar = jsonObject.getString("avatar");
                        Bitmap image = null;
                        if(!avatar.equals("null")){
                            Request imgRequest = new Request.Builder().url(avatar).build();
                            ResponseBody body = client.newCall(imgRequest).execute().body();
                            InputStream in = body.byteStream();
                            image = BitmapFactory.decodeStream(in);
                        }
                        String name = jsonObject.getString("name");
                        String name_zh = jsonObject.getString("name_zh");
                        boolean is_passedaway = jsonObject.getBoolean("is_passedaway");
                        JSONObject scholar_json = jsonObject.getJSONObject("indices");
                        double activity = scholar_json.getDouble("activity");
                        double citations = scholar_json.getDouble("citations");
                        double diversity = scholar_json.getDouble("diversity");
                        double gindex = scholar_json.getDouble("gindex");
                        double hindex = scholar_json.getDouble("hindex");
                        double pubs = scholar_json.getDouble("pubs");
                        double sociability = scholar_json.getDouble("sociability");
                        JSONObject profile_json = jsonObject.getJSONObject("profile");
                        String address="",affiliation="",affiliation_zh="",bio="",edu="",email="",homepage="",note="",
                                position="",work="";
                        if(profile_json.has("address"))
                            address = profile_json.getString("address");
                        if(profile_json.has("affiliation"))
                            affiliation = profile_json.getString("affiliation");
                        if(profile_json.has("affiliation_zh"))
                            affiliation_zh = profile_json.getString("affiliation_zh");
                        if(profile_json.has("bio"))
                            bio = profile_json.getString("bio");
                        if(profile_json.has("edu"))
                            edu = profile_json.getString("edu");
                        if(profile_json.has("email"))
                            email = profile_json.getString("email");
                        if(profile_json.has("homepage"))
                            homepage = profile_json.getString("homepage");
                        if(profile_json.has("note"))
                            note = profile_json.getString("note");
                        if(profile_json.has("position"))
                            position = profile_json.getString("position");
                        if(profile_json.has("work"))
                            work = profile_json.getString("work");
                        Scholar scholar = new Scholar(image,activity,citations,diversity,gindex,
                                hindex,pubs,sociability,name,name_zh,address,affiliation,
                                affiliation_zh, bio,edu,email,homepage,note,position,work,is_passedaway);
                        if(is_passedaway)
                            passedAwayScholarList.add(scholar);
                        else
                            highlightScholarList.add(scholar);
                    }

                    Log.e("this",String.valueOf(highlightScholarList.size()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        mAdapter= new ScholarAdapter(getActivity(), highlightScholarList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(mAdapter);
                    }
                });
            }
        }).start();


        return root;
    }

}
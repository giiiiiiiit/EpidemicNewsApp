package com.java.liyonghui;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.java.liyonghui.ui.data.EpidemicData;
import com.orm.SugarContext;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    //在微博开发平台为应用申请的App Key
    private static final String APP_KY = "990645216";
    //在微博开放平台设置的授权回调页
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    //在微博开放平台为应用申请的高级权限
    private static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
    private IWBAPI mWBAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_news, R.id.navigation_wiki, R.id.navigation_data, R.id.navigation_scholar)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        initSdk();

        SugarContext.init(this);
        InvertedIndex.deleteAll(InvertedIndex.class);
        News.deleteAll(News.class);
        EpidemicData.deleteAll(EpidemicData.class);

        //initInvertedIndex();

        initEpidemicData();


    }


    void initEpidemicData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient();
                    Request.Builder reqBuild = new Request.Builder();
                    HttpUrl.Builder urlBuilder = HttpUrl.parse("https://covid-dashboard.aminer.cn/api/dist/epidemic.json")
                            .newBuilder();
                    reqBuild.url(urlBuilder.build());
                    Request request = reqBuild.build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    JSONObject outerJSON = new JSONObject(responseData);
                    //通过迭代器获得json当中所有的key值
                    Iterator keys = outerJSON.keys();
                    //然后通过循环遍历出的key值
                    while (keys.hasNext()) {
                        String key = String.valueOf(keys.next());
                        boolean isChinaprovinces = isChinaProvince(key);
                        boolean isCountries = isCountry(key);
                        if(isChinaprovinces||isCountries){
                            JSONArray jsonArray = outerJSON.getJSONObject(key).getJSONArray("data");
                            JSONArray jsonAr = jsonArray.getJSONArray(jsonArray.length()-1);
                            int confirmed = jsonAr.getInt(0);
                            int cured = jsonAr.getInt(2);
                            int dead = jsonAr.getInt(3);
                            EpidemicData epidemicData;
                            if(isChinaprovinces){
                                epidemicData = new EpidemicData(key.substring(6),"province",confirmed,cured,dead);
                            }else{
                                epidemicData = new EpidemicData(key,"country",confirmed,cured,dead);
                            }
                            epidemicData.save();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void initInvertedIndex(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dealNews("news");
                dealNews("paper");
            }
        }).start();
    }

    void dealNews(String newsType){
        try{
            OkHttpClient client= new OkHttpClient();
            int num;
            if(newsType.equals("news"))
                num = 2;
            else
                num = 2;
            for(int page=1; page<num; page++){
                Request.Builder reqBuild = new Request.Builder();
                HttpUrl.Builder urlBuilder =HttpUrl.parse("https://covid-dashboard.aminer.cn/api/events/list")
                        .newBuilder();
                urlBuilder.addQueryParameter("page", String.valueOf(page));
                urlBuilder.addQueryParameter("size", "500");
                urlBuilder.addQueryParameter("type", newsType);
                reqBuild.url(urlBuilder.build());
                Request request = reqBuild.build();
                Log.d("this",request.toString());
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject outerJSON = new JSONObject(responseData);
                JSONArray jsonArray = outerJSON.getJSONArray("data");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String seg = jsonObject.getString("seg_text");
                    String id = jsonObject.getString("_id");
                    //Log.d("inverseIndex",newsType+i+seg);
                    String[] words = seg.split(" ");
                    for(String word : words){
                        if(isLetterOrChinese(word)){
                            Log.d("this",word);
                            InvertedIndex invertedIndex = new InvertedIndex();
                            invertedIndex.setIndex(id);
                            invertedIndex.setWord(word);
                            invertedIndex.save();
                        }
                    }
                }

            }

//            List<InvertedIndex> li = Select.from(InvertedIndex.class)
//                    .where(Condition.prop("myword").eq("year")).list();
//            Log.d("this",String.valueOf(li.size()));
//            for(InvertedIndex in :li){
//                String s = in.getIndex();
//                Log.d("InvertedIndex",s);
//            }

    } catch (IOException | JSONException e) {
        e.printStackTrace();
    }

}

    public static boolean isLetterOrChinese(String str) {
        String regex = "^[a-zA-Z\u4e00-\u9fa5]+$";
        return str.matches(regex);
    }

    public static boolean isChinaProvince(String str) {
        String regex = "^China\\|(\\w|\\s)*$";
        return str.matches(regex);
    }

    public static boolean isCountry(String str) {
        String regex = "^(\\w|\\s)*$";
        return str.matches(regex) && !str.equals("World");
    }


    private void initSdk() {
        AuthInfo authInfo = new AuthInfo(this, APP_KY, REDIRECT_URL, SCOPE);
        mWBAPI = WBAPIFactory.createWBAPI(this);
        mWBAPI.registerApp(this, authInfo);
    }
}
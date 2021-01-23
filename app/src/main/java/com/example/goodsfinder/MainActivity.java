package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "artFil";

    private final Handler uiHandler = new Handler();
    boolean isFoundMoyo = false;
    boolean isFoundCitrus = false;
    boolean isFoundRozetka = false;
    int currentStoreIndex;
    SearchView mySearchView;
    ListView goodsListView;
    RecyclerView storeListView;
    ProgressBar progressBar;
    int pageCounter = 1;
    int pageCounterMoyo = 1;
    int pageCounterRozerka = 1;
    int goodsCount = 0;
    int goodsCountMoyo = 0;
    int goodsCountRozetka = 0;
    List<Article> articleList = new ArrayList<>();
    List<Article> articleListMoyo = new ArrayList<>();
    List<Article> articleListRozetka = new ArrayList<>();
    ArrayList<String> goodsNamesList = new ArrayList<>();
    ArrayList<String> goodsPricesList = new ArrayList<>();
    ArrayList<String> goodsImagesList = new ArrayList<>();

    ArrayList<String> goodsImagesListMoyo = new ArrayList<>();
    ArrayList<String> goodsPricesListMoyo = new ArrayList<>();
    ArrayList<String> goodsNamesListMoyo = new ArrayList<>();

    ArrayList<String> goodsImagesListRozetka = new ArrayList<>();
    ArrayList<String> goodsPricesListRozetka = new ArrayList<>();
    ArrayList<String> goodsNamesListRozetka = new ArrayList<>();



    ArrayList<String> storeTitlesList = new ArrayList<>();
    ArrayList<String> storeImagesList = new ArrayList<>();
    String currentRequestString = "";
    TextView errorTextMain;

    private Document doc = null;
    private Document doc1 = null;
    private Document doc2 = null;

    private Thread webThread;
    private Runnable runnable;
    MyAdapter adapterCitrus;
    MyAdapter adapterMoyo;
    MyAdapter adapterRozetka;

    WebView browser;
    WebView browserMoyo;
    WebView browserRozetka;

    public int getCurrentStoreIndex() {
        return currentStoreIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isFoundMoyo = false;
        isFoundCitrus = false;
        isFoundRozetka = false;
        browser = new WebView(this);
        browserMoyo = new WebView(this);
        browserRozetka = new WebView(this);
        currentStoreIndex = 0;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initRecyclerView();

        errorTextMain = findViewById(R.id.errorTextMain);
        errorTextMain.setVisibility(View.INVISIBLE);
        mySearchView = (SearchView) findViewById(R.id.goodsSearchView);
        goodsListView = (ListView) findViewById(R.id.goodsList);
        storeListView = (RecyclerView) findViewById(R.id.storeListView);
        progressBar = findViewById(R.id.progressBar);
        adapterCitrus = new MyAdapter(this, goodsNamesList, goodsPricesList, goodsImagesList,articleList);
        adapterMoyo = new MyAdapter(this, goodsNamesListMoyo, goodsPricesListMoyo, goodsImagesListMoyo,articleListMoyo);
        adapterRozetka = new MyAdapter(this, goodsNamesListRozetka, goodsPricesListRozetka, goodsImagesListRozetka,articleListRozetka);
        goodsListView.setAdapter(adapterMoyo);


        getMoyo();
        getRozetka();


        storeTitlesList.add("MoYo");
        storeTitlesList.add("Citrus");
        storeTitlesList.add("Allo");


        storeImagesList.add("https://img.moyo.ua/img/logos/2020/LOGO_site-min_1608538727.jpg");
        storeImagesList.add("https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");
        storeImagesList.add("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWwAAACLCAMAAAByd1MFAAAAyVBMVEXvNj/////uJC/uKTT5v8D2nKDvMz3vLDfvMjz1jIX6zc7vLzr1iH/uJzP3pp/3qKH5w73+8uzwTFP/+/nzb2b96+buIC3+9fL5x8jwPkPxWVb96OPzdXDuHynwPj72oZ73p6r0e3T70s/84dv82tXyZ2P5tq/wSUn1lI7zbmX96ej6y8X4r6fya2v0hYPuEiL4uLb2l4/wR0P82tPyY1r3sbLxVE7zfHv71NXzc3n1kZXxWWDyZVz4uLjwR0HxWVD2lpr0fIH94ePyIPO0AAAPF0lEQVR4nO1da3vaOBMFOyuZGnENEBMgIUBIc2tIUpq2abvb//+jXjvlYh3dCbgfXp3n2Q/b4LF1LB3NjEZyqexRGEp/+wH+n+DJLhCe7ALhyS4QnuwC4ckuEGuyr4883o0PL9dWZJ98CjzeC0aj5O78xUj2B1by2ANIzGjrc8+TXRQilvwjpduTfQgQlvzwZBeGiH0VO7cn+1AIW8JM6ck+GOLSB092YYiQbU/2ARGVXjzZhSFuXXuyCwP96skuDuyzJ7swRMm1J7swhN882YWBlK492YWB/ePJLgxRy5NdHOiLJ7swbHXEk31wRN892YWBlHqe7MKwEW1P9uHBTjzZhSE48mQXhoOQTUgUReQ9F2fX72rgcBZTOzGlNEz/i3exxs73R3baqJiGAYtLSavVSuIgdnweEjMWJdnF2fVJ+n+uFmQW47XFeRLtbJFENKBJazaoVitn1Wr1uD9PQkYjJyP76NlvJLOAlpL+cbV2dDHpZC5Oc9IYk9ChaRFLxpcXk+bKP2p2Jhf144S9g24SJsf19HGafyz2mpOLh3HC3Bgqvb2y0vPiZNos59DuTB+qLepi7T1kZySnHZmW5s/jxcOHaaddBnQWNLa1RueXwvUp5fWRtQVEPKpJ6pHajXnoZIaE8aDRFA29YXLbD6mtpZ3IXpOcpCSnHVn1IBlO+5YtY18kVL/R/dGNmw3orCO32L4P7K2QcHSmsLPCdMws+4M72VEqgmaStxhYcRUs1BbGO7FNB2qLZ9bDNw7PFMWROUwGdmw7k52Odv2LFvBs8SR0rLMw20FJ4pnO4r3teJud2jTxqWtlzZVsVrW5OYdeYJzjorlCQ/7gNHGeJcloorPYXlrNa13NeMvjxk6XHMmmX+zuzuHS+CjsRm/h1kFkVxbrBnpshGR4adnCcztZciObjMwCJsHI0DHjY4OBXt/RXYtapge1kNmhoQts8XoIzaaWwwpwa3jx7MJkwTw4oFnGPnlh7IzdW9v2nQ4tn8qJbDMrUnT080f8bLTQmzupNpmbR6Bp1g3tZydLZ8SR7Fg766ih15HgwWxh4eT+hVdmiw39YIkerVu3sPNFnGVkav0EHLSvniQWDvvESUcCi07R07s4n2z7VXtsy7Uj2YFyymhObh7Oq68/g0/hWHwjFV1Eq/ex13DxtaPvWk9yhbH2oSxFZLIIrKN1V83GAd95uvlxVn1+DLpBwBjNUmqEDiv4SFqy1W8wj5qDjoQ1G4ta53goG22Th2r/kf5cfjyuXH3+/Pm88hp27amWkH2ke4SVN9KcXDQ+Xw1+jVKCgyCkmLYM0BvXkU0SjEjbl9XxLc5wLjoiqkijOqhjLKibdKnQX8q9y3mX0YikiGNKWba1FBtOYq2L6kZ2NKuNX5chSzlmWf5cabVhT3aMOYzTkKVtocBN+7u1qx31QUV6yy6N2RAeSjeTdIXX9TAyJVMJG45mWrVzI7sUpd3YotFR355sfDHlPznV+BEYO7PWkRCjgeO3S8knGEJqfyT6BRbaY2POIXisp29oohVMN7JtEfKTpIZsMoIuvI6AAogppvaTPszQN6uYAye9jtIfwbmp/Wx609E6jNLFugciG1xnDdnxHXSitU+Oo6NtG7JHfdD7jVwMy4o/IBiMgZlpGoxG6yvqGgfjUD2b9wc0ZGMn2mYrh9DlbeManNx6mxZ1we95ULQ1WvK/uzKG9sPN25nqFPNAZPNJNzXZgopcbX7KQEcuLHWkCzmFrTSjR3+qiGzDM+5nU+NbZrlUjMbH+dtkCzn+bfvxTz27HHSEeZHxViwsdQQyvgOTiJCR2WaGv002dt9JLoE2BOW8sgog6D28o1y/xE6vSEeG3Gg7NVY/cCNBk+L8y2STCBzavIPHIE9qpyMozHkHD+VcriMk4X70YFRs9pR/yqLJJoHlBBmhivzMtR7Dnd6jRZ6VPIKK3OfuTUK43Z1szMcfud+YE6hBPiQ4LU6zSURDNqRLvlMqycbVqwmXhg8gQXFvoSOCinCdt/vE/1XqqEH/n8NUQTByJj/zP2+rs4n7IzujORiOnu/PGpMeRH9KsimoCN941JEbCx3pQkDa4K6hkOiWBnyMO/yGJy8OwuVy1KX5f4v4xQ91PLAXsklMWXfU/3LWuEaa9WQLuVDe4YghodUzLWZKVkn5W3N+QwZZLiM4yf+iWdrelASDRq/X7k2v8pVesIR6rJSdd5MdZTzPKrfTnmYlSkU2ZjFgMY+EYPOLUUdw+b8NOj8EHZFlbvnFvxzZ8WgzbE5n2wtB46vKh3wX2SnRQf++PpV3ZxuyIYuBbhMmqQxLWSVxiQ2VB+IVacASci9kSzZXi9K727QJJnJ1xmx3suOwu7y/vLaqbVCQLeRCsSQAZ7umSUfIyOCbkzn/d1nmlnI2tmTzr377LDChqpc5diQ7CoNZbWJdRKIgG6erJpYECAr8xeCGocq3heB5CFOyJOMSc07Qhmz0RDfDEDRbzeBOZMfdfu3UZplvDQXZmAsVgy/0LVSpozXQfxGL8NDZnIoWFWSjpvXWxeeg2SdKn2kHsuNgcOHCdFlFtlC2JM7jqCMdQ3V9CAfOihE+Zm4lxVYKskNcvlt7MtCz90g2Ce7c6xnkZAu5ULGwiOB6jT6cw5Euq5/EzK3wPgh/uvWabLHw59vqyoORHY9siw3zkJMdQFpIloTATIe+kk0ISCXtxtTXi9DgiHvBG7KXRZMdz3eqiZKSLaiILFGBc2hH+3gBqIjMCxMyty3s/XIZEYtKB4eVkXhpWQjf5ke/lGzU47bszli53dYtXyOP8pU0zNwKD6fQbMzPbuIlJHs/3oiwrCJD7/rm7L8+P1alZGNxjjxi6cIMoVvjY1CcI1MRi8xtLPezcYrZxEtAdn0/fjY+JqJ33Tj7smTdkAbmfLYw4chjcdSRa83zYbWHPLzAWbSJvrgqggw589thA2TvJ6hBrwl4XgyWWXnU2yNYLB4IWQz5jTHK1CyyCz9VSE6gyXi/PbyC7Pg5f+F2FRj8bPWylAvZio6d9efBvBuEueUjC7Jt8x6oI+rUA+Y9VBVrpswtvwSZS0TlNvu1r7bGoWeP95L1i0TFbt6kusHz/KfdRrLJyHJlADODciHOYCvvqCMdyLjw+exJjjua3Daz0dO7meVsg5i/7iOfnTu9a4XGXaYbspjOTLagIlI7JQcdiZYQAEnXvN4aDT+E2YKfJp7yfySMzu6/DX4GedMwonBlJ3dfe7JBmtLx0lUOGDPZ9qswWJOqKtbBIaCeSg0KxjcUPDkSUyxe3XD4BnVRmwvZWCo31jhhRrKNudCcLXDoVEV/6AernUT08K95HeFX1xfGBYuAk1dNSY8D2dBobWGBkWwhi/FT2R/iGeiIvOZISKMoVSR90/qMC81PJ8YSQyhW00x6DmRDVZ52l4SRbJfaMgzC5YNAcMhVk0BJzLhc8oMgvy42cao9k7Z12xB7srvcOqh+B5iRbHMuNNcaSC/JX4yBQA5C5pZ/MfkZT9unMkQwTDQjYXeytRkhE9lYKKzdSy78WCY5JmnQ/5iXnFwtvHlXOsy2Hc1xRn+JbOysT9o9sgyGgTQghcwFOs/QFn3RH1uL9tS44QBPTmjspz4byNYmlk1kB5jB1yqjjZuoK/ETYci4rKen266Ra9wRpJMdB7J5d7L8SzdLG8gWHAx9NbCwx0nstWKJiXZJB10XSKPEr9m/XS5NGkIC3C6p3cq6u+unPY3GQLaQxdDvtCcUdERMEGJAqlcRSbEO35qwfrUcmreHjXDzlVZF3hHUdHQEGcjGXKhpJ5g5aSX8wlAWiLvbT8FiKJwiR+IMm+P8ojAYSY5t0HrlLuH6K2+3NlR3Hj3ZDmnT1a0xkSIssg8NaVMEJlIMj0CD7uj5Y4pfj6NhN0XwvJCdUDHVzmQOZJME/KXb7Lby76p+0tZno4oYz+sQgnv064QFAWMpNxbr6AYXDe5vci+z3REP1VtBfx6WS4oVK/NSj+3p6aYhBd8UINtlqWt1BagERizor5hURDJtqK/oVmxLv7SK7bh4YDh3SQ2ebGEIm09cwPkPS0xcVUSUMvX2KOuTosptw6y8r2UxPXiyhVyo+dQfMoelBr54SihPsDh4Z4gJA4WO4CqyBqbQ3m3Bd8ezXYBs+1xo7hqtjuyy1Uko+pNfg2WvGtRNbrkT2fqDCTXgluWEZXV1LnQLfakang6iPd9k0xjQEflowPSkGuZ5wq1IhwlOvB24zJGQxbApUhbeUL6UWyiWtNqe2rXIuIilgUqYuXYkm8S2t+bQ5J4Dsxh2Z1tg1JJPHWHvM29QyIBTh1x7cDOfCjUL5XI9b+RxF7a5shWsmTMF1us7g/eQD2DhIBHL8xsIX/kk32ZJ7A5Ba97ZvF/XKtYotj7Gcdt4Lk+JiXtjdn4FBgH2llH3QxRWTwJ5cpm/SEY27b2NrRrhXp/NqnZHOW/Bb7QHPahYn64MPthWfSA+qdlb5GcP6QI/YTPDyZHty6Xl8fU77DygUcXJBaxyjSc0ryIdu9O1/4BV85duM4Vc5N2ruByTdpdXEsU2y5gt6+pC6emV/bn+u+ypIZS2rj5MrLZ6tBtwNHxeRSZnI5eT2ko0qefIWSfU855w53LuZrG0yPGoijsjRluVE6HBzenDeB46fNZh191iNIjm/Y9VE46F1x6P67UM9atqP3L5/kQGEpZm1cXKwPPKctRf/cOiOnP+egQJo361sjKgrtJbNXhcqT2cZKjXqsethDp+zkMg+4f1plPy9tEWPSRHdcThH1DH746sb0rpysB2gtxa3OXTH1uLhuDq7csw7C3BykLdSXsqHOi8EQ8ZPNkFQiD7xH9b7GAQyH5xms09XCCQfV3aZZLxsIFAdlnYFuixL4hkf9vx61IeRohk+xnyYBDJNnwLwGN3iGR7HTkYJGT/9s7fgSAhu/zVs30YyMj+/d4v53rIISO7/K93SA4CKdnt7zt/N9dDAynZ5Rfiw8gDQE52+eg9X+D2UEBBdvncs71/qMj2bB8ASrLLP2I/S+4ZarLLLy0ft+8XGrLL7W/Ux5L7hI7stHPfyQ/K8dgJerJTuv8rBZ7vPcFEdrncO/qaxCwIPd6NT0ayM/z+cPSPx7vx728bsj32C092gfBkFwhPdoHwZBcIT3aB8GQXiP8BSJdACFyx5yEAAAAASUVORK5CYII=");


        storeListView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, storeListView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        if (position == 0) {
                            currentStoreIndex = 0;
                            goodsListView.setAdapter(adapterMoyo);

                        } else if (position == 1) {
                            currentStoreIndex = 1;
                            goodsListView.setAdapter(adapterCitrus);

                        }else if (position == 2) {
                            currentStoreIndex = 2;
                            goodsListView.setAdapter(adapterRozetka);

                        }
                       initRecyclerView();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentStoreIndex == 1) {
                    Element loadElement = doc.getElementsByAttributeValue("class", "catalog-card-container more-items product-card product-card--mini").first();

                    if (loadElement == null ^ i != articleList.size() - 1) {

                        Uri address = Uri.parse(articleList.get(i).getUrl());
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);
                    } else {

                        if (isConnected()) {

                            articleList.remove(articleList.size() - 1);
                            goodsPricesList.remove(goodsPricesList.size() - 1);
                            goodsNamesList.remove(goodsNamesList.size() - 1);
                            goodsImagesList.remove(goodsImagesList.size() - 1);
                            pageCounter++;
                            getCitrus(currentRequestString + "&page=" + pageCounter);

                            progressBar.setVisibility(View.VISIBLE);
                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsImagesListMoyo.clear();
                            goodsPricesListMoyo.clear();
                            articleList.clear();
                            articleListMoyo.clear();
                            adapterCitrus.notifyDataSetChanged();
                            adapterMoyo.notifyDataSetChanged();
                            currentRequestString = "";
                            pageCounter = 1;
                            pageCounterMoyo = 1;
                            goodsCount = 0;
                            goodsCountMoyo = 0;
                        }
                    }
                } else if (currentStoreIndex == 0) {
                    Element loadElement = doc1.getElementsByAttributeValue("class", "new-pagination-more").first();

                    if (loadElement == null ^ i != articleListMoyo.size() - 1) {
                        Log.d(TAG, articleListMoyo.size() - 1 + "------" + i);
                        Uri address = Uri.parse(articleListMoyo.get(i).getUrl());
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);
                    } else {

                        if (isConnected()) {
                            articleListMoyo.remove(articleListMoyo.size() - 1);
                            goodsPricesListMoyo.remove(goodsPricesListMoyo.size() - 1);
                            goodsNamesListMoyo.remove(goodsNamesListMoyo.size() - 1);
                            goodsImagesListMoyo.remove(goodsImagesListMoyo.size() - 1);
                            pageCounterMoyo++;

                            getMoyoGoods(currentRequestString + "&page=" + pageCounterMoyo);
                            progressBar.setVisibility(View.VISIBLE);


                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsImagesListMoyo.clear();
                            goodsPricesListMoyo.clear();
                            articleList.clear();
                            articleListMoyo.clear();
                            adapterCitrus.notifyDataSetChanged();
                            adapterMoyo.notifyDataSetChanged();
                            currentRequestString = "";
                            pageCounter = 1;
                            pageCounterMoyo = 1;
                            goodsCount = 0;
                            goodsCountMoyo = 0;
                        }
                    }
                }else if (currentStoreIndex == 2) {
                    Element loadElement = doc2.getElementsByAttributeValue("class", "pagination__next__link").first();

                    if (loadElement == null ^ i != articleListRozetka.size() - 1) {
                        Log.d(TAG, articleListRozetka.size() - 1 + "------" + i);
                        Uri address = Uri.parse(articleListRozetka.get(i).getUrl());
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);
                    } else {

                        if (isConnected()) {
                            articleListRozetka.remove(articleListRozetka.size() - 1);
                            goodsPricesListRozetka.remove(goodsPricesListRozetka.size() - 1);
                            goodsNamesListRozetka.remove(goodsNamesListRozetka.size() - 1);
                            goodsImagesListRozetka.remove(goodsImagesListRozetka.size() - 1);
                            pageCounterRozerka++;

                            getRozetkaGoods("https://allo.ua/ru/catalogsearch/result/index/p-"+pageCounterRozerka+"/?q="+currentRequestString);
                            progressBar.setVisibility(View.VISIBLE);


                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsImagesListMoyo.clear();
                            goodsPricesListMoyo.clear();
                            goodsNamesListRozetka.clear();
                            goodsImagesListRozetka.clear();
                            goodsPricesListRozetka.clear();
                            articleList.clear();
                            articleListMoyo.clear();
                            articleListRozetka.clear();
                            adapterCitrus.notifyDataSetChanged();
                            adapterMoyo.notifyDataSetChanged();
                            adapterRozetka.notifyDataSetChanged();
                            currentRequestString = "";
                            pageCounter = 1;
                            pageCounterMoyo = 1;
                            pageCounterRozerka = 1;
                            goodsCount = 0;
                            goodsCountMoyo = 0;
                            goodsCountRozetka = 0;
                        }
                    }
                }


            }

        });


        progressBar.setVisibility(View.GONE); // to hide
        mySearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySearchView.setIconified(false);
            }
        });

        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                currentRequestString = "";
                pageCounterMoyo = 1;
                pageCounter = 1;
                pageCounterRozerka = 1;
                goodsCountMoyo = 0;
                goodsCount = 0;
                goodsCountRozetka = 0;
                goodsNamesList.clear();
                goodsImagesList.clear();
                goodsPricesList.clear();
                goodsNamesListMoyo.clear();
                goodsImagesListMoyo.clear();
                goodsPricesListMoyo.clear();
                goodsNamesListRozetka.clear();
                goodsImagesListRozetka.clear();
                goodsPricesListRozetka.clear();
                articleList.clear();
                articleListMoyo.clear();
                articleListRozetka.clear();
                if (isConnected()) {
                    errorTextMain.setVisibility(View.INVISIBLE);
                    currentRequestString = s;
                    adapterCitrus.notifyDataSetChanged();
                    adapterMoyo.notifyDataSetChanged();
                    adapterRozetka.notifyDataSetChanged();

                    progressBar.setVisibility(View.VISIBLE); //to show

                    getCitrus(s);
                    getMoyoGoods(s);
                    getRozetkaGoods("https://allo.ua/ru/catalogsearch/result/?q="+s);



                } else {
                    errorTextMain.setVisibility(View.VISIBLE);
                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        mySearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                goodsNamesList.clear();
                goodsImagesList.clear();
                goodsPricesList.clear();
                goodsNamesListMoyo.clear();
                goodsImagesListMoyo.clear();
                goodsPricesListMoyo.clear();
                goodsNamesListRozetka.clear();
                goodsImagesListRozetka.clear();
                goodsPricesListRozetka.clear();
                articleList.clear();
                articleListMoyo.clear();
                articleListRozetka.clear();
                adapterCitrus.notifyDataSetChanged();
                adapterMoyo.notifyDataSetChanged();
                adapterRozetka.notifyDataSetChanged();
                currentRequestString = "";
                pageCounter = 1;
                pageCounterMoyo = 1;
                pageCounterRozerka = 1;
                goodsCount = 0;
                goodsCountRozetka = 0;
                return false;
            }
        });


    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.storeListView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, storeTitlesList, storeImagesList);
        recyclerView.setAdapter(adapter);
    }





    boolean isConnected() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else
            connected = false;

        return connected;
    }




    void getCitrus(String s){

        runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    doc = Jsoup.connect("https://www.citrus.ua/search?query="+s).get();
                    Elements elements = doc.getElementsByAttributeValue("class", "short-itm-desc");

                    elements.forEach(image-> {
                        Element aElement = image.child(5);
                        Element imgElement = image.child(2);
                        Element priceElement = image.child(7);
                        String url = "https://www.citrus.ua"+aElement.attr("href");
                        String title = aElement.child(0).text();
                        String price = "";

                        if (priceElement.child(1).childrenSize()>1){
                            if (priceElement.child(1).child(1).attr("class")=="price-not-ready"){
                                price = "Цена формируется";
                            } else {
                                price = priceElement.child(1).child(1).child(0).text().replaceAll("грн","");
                            }
                        } else {
                            if (priceElement.child(1).child(0).attr("class")=="price-not-ready"){
                                price = "Цена формируется";
                            } else {

                                price = priceElement.child(1).child(0).text().replaceAll("грн","") ;
                            }
                        }

                        Document doc2 = null;
                        try {
                            doc2 = Jsoup.connect(url).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Elements images = doc2.getElementsByAttributeValue("class", "gallery").select("img");
                        String img = images.attr("src");




                        articleList.add(new Article(url, title, price+ " грн",  img));
                    });

                    for (int i = goodsCount; i < articleList.size(); i++) {
                        goodsNamesList.add(articleList.get(i).getName());
                        goodsPricesList.add(articleList.get(i).getPrice() );

                        goodsImagesList.add(articleList.get(i).getImg());



                    }
                    goodsCount = articleList.size();
                    Element loadElement = doc.getElementsByAttributeValue("class", "catalog-card-container more-items product-card product-card--mini").first();

                    if (loadElement != null) {
                        goodsNamesList.add("Загрузить ещё");
                        goodsPricesList.add("");

                        goodsImagesList.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                        articleList.add(new Article(" ", "Загрузить ещё", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE); // to hide
                            adapterCitrus.notifyDataSetChanged();
                            goodsListView.setEnabled(true);
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG,"CITRUS ERROR");
                }



            }
        };


        webThread = new Thread(runnable);
        webThread.start();

    }



    void getMoyoGoods(String search) {
        goodsListView.setEnabled(false);
        isFoundMoyo = false;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String search1 = search.replace(" ", "+");
                browserMoyo.loadUrl("https://www.moyo.ua/search/new/?q=" + search1);

            }
        });

    }

    void getMoyo() {
        try {
            isFoundMoyo = false;

            browserMoyo.setVisibility(View.INVISIBLE);
            browserMoyo.setLayerType(View.LAYER_TYPE_NONE, null);
            browserMoyo.getSettings().setJavaScriptEnabled(true);
            browserMoyo.getSettings().setBlockNetworkImage(false);
            browserMoyo.getSettings().setDomStorageEnabled(true);
            browserMoyo.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            browserMoyo.getSettings().setLoadsImagesAutomatically(true);
            browserMoyo.getSettings().setGeolocationEnabled(false);
            browserMoyo.getSettings().setSupportZoom(false);
            browserMoyo.getSettings().setBlockNetworkLoads(false);
            browserMoyo.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            browserMoyo.addJavascriptInterface(new JSHtmlInterfaceMoyo(), "JSBridge");

            browserMoyo.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {

                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            browserMoyo.loadUrl("javascript:window.JSBridge.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Moyo Error");
        }


    }

    private class JSHtmlInterfaceMoyo{
        @android.webkit.JavascriptInterface
        public void showHTML(String html) {
            final String htmlContent = html;

            uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!isFoundMoyo) {

                                doc1 = Jsoup.parse(htmlContent);
                                Elements elements = doc1.getElementsByAttributeValue("class", "product-tile_inner");

                                boolean isConsist = false;
                                for (Element element : elements) {
                                    String url = element.getElementsByAttributeValue("class", "product-tile_title ddd").first().child(0).attr("href");
                                    String price = element.getElementsByAttributeValue("class", "product-tile_price-value").first().text();
                                    String title = element.getElementsByAttributeValue("class", "product-tile_title ddd").first().text();
                                    String imgUrl = element.getElementsByAttributeValue("class", "goods_image product-tile_image").first().child(0).attr("src");


                                    articleListMoyo.add(new Article("https://www.moyo.ua/" + url, title, price, imgUrl));


                                }

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        for (int i = goodsCountMoyo; i < articleListMoyo.size(); i++) {
                                            goodsNamesListMoyo.add(articleListMoyo.get(i).getName());
                                            goodsPricesListMoyo.add(articleListMoyo.get(i).getPrice() + " грн");

                                            goodsImagesListMoyo.add(articleListMoyo.get(i).getImg());


                                        }
                                        goodsCountMoyo = articleListMoyo.size();
                                        Element loadElement = doc1.getElementsByAttributeValue("class", "new-pagination-more").first();

                                        if (loadElement != null) {
                                            goodsNamesListMoyo.add("Загрузить ещё");
                                            goodsPricesListMoyo.add("");

                                            goodsImagesListMoyo.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                                            articleListMoyo.add(new Article(" ", "Загрузить ещё", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                                        }
                                        progressBar.setVisibility(View.GONE); // to hide


                                    }
                                });

                                Log.d(TAG, articleListMoyo.size() + "  " + goodsImagesListMoyo.size());

                                isFoundMoyo = true;
                                adapterMoyo.notifyDataSetChanged();
                                goodsListView.setEnabled(true);
                            }
                        }
                    }
            );
        }
    }




    void getRozetkaGoods(String search) {
        goodsListView.setEnabled(false);
        isFoundRozetka = false;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String search1 = search.replace(" ", "%20");
                browserRozetka.loadUrl(search1);

                if(getSupportActionBar()!=null) getSupportActionBar().setTitle(browserRozetka.getUrl());
                Log.d(TAG,"Step 2");
            }
        });

    }

    void getRozetka() {
        try {

            isFoundRozetka = false;
            Log.d(TAG,"Step 1");
            browserRozetka.setWebChromeClient(new WebChromeClient());
            browserRozetka.setVisibility(View.INVISIBLE);
            browserRozetka.setLayerType(View.LAYER_TYPE_NONE,null);
            browserRozetka.setWebChromeClient(new WebChromeClient());
            browserRozetka.getSettings().setJavaScriptEnabled(true);
            browserRozetka.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            browserRozetka.getSettings().setDomStorageEnabled(true);
            browserRozetka.getSettings().setBlockNetworkLoads(false);
            browserRozetka.getSettings().setBlockNetworkImage(false);
            browserRozetka.getSettings().setLoadWithOverviewMode(true);
            browserRozetka.getSettings().setUseWideViewPort(true);
            browserRozetka.getSettings().setDefaultTextEncodingName("utf-8");

            browserRozetka.addJavascriptInterface(new JSHtmlInterfaceRozetka(), "JSBridge");

            browserRozetka.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {

                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            browserRozetka.loadUrl("javascript:window.JSBridge.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Rozetka Error");
        }


    }

    private class JSHtmlInterfaceRozetka{
        @android.webkit.JavascriptInterface
        public void showHTML(String html) {
            final String htmlContent = html;

            uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (!isFoundRozetka) {
                                Log.d(TAG, "Step 3");
                                Log.d(TAG, htmlContent);
                                doc2 = Jsoup.parse(htmlContent);
                                Elements elements = doc2.getElementsByAttributeValue("class", "product-card");
                                for (Element element : elements) {
                                    String title = element.getElementsByAttributeValue("class", "product-card__title").first().text();
                                    String url = element.getElementsByAttributeValue("class", "product-card__title").first().attr("href");
                                    String price = element.getElementsByAttributeValue("class", "sum").last().text();
                                    // String imgUrl = String.valueOf(element.getElementsByAttributeValue("class", "goods-tile__picture").first().childrenSize());
                                    String imgUrl = element.getElementsByAttributeValue("class", "gallery__img-wrapper").first().select("img").attr("data-src");

                                    articleListRozetka.add(new Article(url, title, price, imgUrl));


                                }

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        for (int i = goodsCountRozetka; i < articleListRozetka.size(); i++) {
                                            goodsNamesListRozetka.add(articleListRozetka.get(i).getName());
                                            goodsPricesListRozetka.add(articleListRozetka.get(i).getPrice() + " грн");

                                            goodsImagesListRozetka.add(articleListRozetka.get(i).getImg());
                                            Log.d(TAG, articleListRozetka.get(i).toString());

                                        }
                                        goodsCountRozetka = articleListRozetka.size();
                                        Element loadElement = doc2.getElementsByAttributeValue("class", "pagination__next__link").first();

                                        if (loadElement != null) {
                                            goodsNamesListRozetka.add("Загрузить ещё");
                                            goodsPricesListRozetka.add("");

                                            goodsImagesListRozetka.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                                            articleListRozetka.add(new Article(" ", "Загрузить ещё", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                                        }
                                        progressBar.setVisibility(View.GONE); // to hide


                                    }
                                });


                                isFoundRozetka = true;
                                adapterRozetka.notifyDataSetChanged();
                                goodsListView.setEnabled(true);
                            }
                        }
                    }
            );
        }
    }







    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<Article> arrLIst = new ArrayList<>();


        MyAdapter(Context c, List<String> title, List<String> description, List<String> imgs, List arrList) {
            super(c, R.layout.goods_view, R.id.goodsTitleMain, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rImgs = imgs;
            this.arrLIst = arrList;

        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View goodsView = layoutInflater.inflate(R.layout.goods_view, parent, false);
            ImageView images = goodsView.findViewById(R.id.goodsImageMain);
            ImageView imagesFavourite = goodsView.findViewById(R.id.goodsFavourite);
            TextView myTitle = goodsView.findViewById(R.id.goodsTitleMain);
            TextView myDescription = goodsView.findViewById(R.id.goodsPriceMain);

            // now set our resources on views


            Glide.with(context).load(rImgs.get(position)).into(images);

            myTitle.setText(rTitle.get(position));


            if (rTitle.get(position) == "Загрузить ещё") {
                imagesFavourite.setVisibility(View.GONE);
                myTitle.setTypeface(null, Typeface.BOLD_ITALIC);
                myTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }
            myDescription.setText(rDescription.get(position));

            imagesFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG,arrLIst.get(position).getUrl());
                    



                }
            });

            return goodsView;
        }
    }


    class Article {
        private String url;
        private String name;
        private String img;
        private String price;


        public Article(String url, String name, String price, String img) {
            this.url = url;
            this.name = name;
            this.price = price;
            this.img = img;
        }


        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String name) {
            this.price = price;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        @Override
        public String toString() {
            return "Article{" +
                    "url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    ", img='" + img + '\'' +
                    ", price='" + price + '\'' +
                    '}';
        }
    }

    static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);

            public void onLongItemClick(View view, int position);
        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mListener != null) {
                        mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private static final String TAG = "RecyclerViewAdapter";

        //vars
        private ArrayList<String> mNames = new ArrayList<>();
        private ArrayList<String> mImageUrls = new ArrayList<>();
        private Context mContext;

        public RecyclerViewAdapter(Context context, ArrayList<String> names, ArrayList<String> imageUrls) {
            mNames = names;
            mImageUrls = imageUrls;
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder: called.");


            Glide.with(mContext)
                    .asBitmap()
                    .load(mImageUrls.get(position))
                    .into(holder.image);

            holder.name.setText(mNames.get(position));


            if(position == currentStoreIndex){
                holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);


                holder.image.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                holder.image.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
            }
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }



        @Override
        public int getItemCount() {
            return mImageUrls.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView image;
            TextView name;

            public ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.storeImageMain);
                name = itemView.findViewById(R.id.storeTitleMain);
            }
        }
    }
}
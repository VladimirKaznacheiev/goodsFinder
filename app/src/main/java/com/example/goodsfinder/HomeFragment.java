package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.view.MenuItem;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;




public class HomeFragment extends Fragment{

    RecyclerView recyclerView;


    String BlockAlloClass;
    String TitleAlloClass;
    String UrlAlloClass;
    String PriceAlloClass;
    String OldPriceAlloClass;
    String ImageAlloClass;
    String LoadMoreAlloClass;

    String BlockRozetkaClass;
    String TitleRozetkaClass;
    String UrlRozetkaClass;
    String PriceRozetkaClass;
    String OldPriceRozetkaClass;
    String ImageRozetkaClass;
    String LoadMoreRozetkaClass;

    String BlockCitrusClass;
    String TitleCitrusClass;
    String UrlCitrusClass;
    String PriceCitrusClass;
    String OldPriceCitrusClass;
    String ImageCitrusClass;
    String LoadMoreCitrusClass;

    DialogFragment dlg;
    DialogFragment dlg2;
    ListView listSearches;
    public ArrayList<String> searches = new ArrayList<>();

    public boolean isCatch = false;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    FirebaseUser user = mAuth.getInstance().getCurrentUser();

    public boolean isFocusedSearch = false;

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

    boolean stopHandlers = false;

    boolean isStoreLoaded = false;

    public String strForSearch;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    List<Article> articleList = new ArrayList<>();
    List<Article> articleListMoyo = new ArrayList<>();
    List<Article> articleListRozetka = new ArrayList<>();
    ArrayList<String> goodsNamesList = new ArrayList<>();
    ArrayList<String> goodsPricesList = new ArrayList<>();
    ArrayList<String> goodsOldPricesList = new ArrayList<>();
    ArrayList<String> goodsImagesList = new ArrayList<>();
    ArrayList<Integer> goodsColorsList = new ArrayList<>();

    ArrayList<String> goodsImagesListMoyo = new ArrayList<>();
    ArrayList<String> goodsPricesListMoyo = new ArrayList<>();
    ArrayList<String> goodsOldPricesListMoyo = new ArrayList<>();
    ArrayList<String> goodsNamesListMoyo = new ArrayList<>();
    ArrayList<Integer> goodsColorsMoyo = new ArrayList<>();

    ArrayList<String> goodsImagesListRozetka = new ArrayList<>();
    ArrayList<String> goodsPricesListRozetka = new ArrayList<>();
    ArrayList<String> goodsOldPricesListRozetka = new ArrayList<>();
    ArrayList<String> goodsNamesListRozetka = new ArrayList<>();
    ArrayList<Integer> goodsColorsRozetka = new ArrayList<>();






    ArrayList<String> storeTitlesList = new ArrayList<>();
    ArrayList<Integer> storeImagesList = new ArrayList<>();
    ArrayList<Integer> storeColorsList = new ArrayList<>();
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
    HistoryAdapter adapterHistory;

    WebView browser;
    WebView browserMoyo;
    WebView browserRozetka;

    public boolean isLoadedCitrus = false;
    public boolean isLoadedRozetka = false;
    public boolean isLoadedAllo = false;


    public int getCurrentStoreIndex() {
        return currentStoreIndex;
    }

    SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "searches";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home2, container, false);
        recyclerView = view.findViewById(R.id.storeListView);


        dlg = new Dialog();
        dlg2 = new Dialog2();

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = mSettings.edit();

/*        editor.clear();
        editor.apply();*/

        if (mSettings.getString("theme", "").equals("")) {
            editor.putString("theme", "light");
            editor.apply();
            editor.commit();
        } else if (mSettings.getString("theme", "").equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (mSettings.getString("theme", "").equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.child("Class").child("PageProduct").child("Allo").addValueEventListener(new ValueEventListener() {
            public boolean inChosen;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();
                Log.d("DATA", "DATA1");

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                    Map<String,Object> taskMap = new HashMap<String,Object>();
                    objRef.updateChildren(taskMap);

                    String tmp = String.valueOf(childDataSnapshot.getValue());
                    String tmp2 = String.valueOf(childDataSnapshot.getKey());
                    if (tmp2.equals("Block")){
                        BlockAlloClass = tmp;
                    } else if (tmp2.equals("Title")){
                        TitleAlloClass = tmp;
                    } else if (tmp2.equals("Url")){
                        UrlAlloClass = tmp;
                    } else if (tmp2.equals("OldPrice")){
                        OldPriceAlloClass = tmp;
                    } else if (tmp2.equals("Image")){
                        ImageAlloClass = tmp;
                    } else if (tmp2.equals("LoadMore")){
                        LoadMoreAlloClass = tmp;
                    } else if (tmp2.equals("Price")){
                        PriceAlloClass = tmp;
                    }
                }
                Log.d("DATA", BlockAlloClass);
                Log.d("DATA", TitleAlloClass);
                Log.d("DATA", UrlAlloClass);
                Log.d("DATA", OldPriceAlloClass);
                Log.d("DATA", ImageAlloClass);
                Log.d("DATA", LoadMoreAlloClass);
                Log.d("DATA", PriceAlloClass);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("Class").child("PageProduct").child("Citrus").addValueEventListener(new ValueEventListener() {
            public boolean inChosen;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();
                Log.d("DATA", "DATA2");

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                    Map<String,Object> taskMap = new HashMap<String,Object>();
                    objRef.updateChildren(taskMap);

                    String tmp = String.valueOf(childDataSnapshot.getValue());
                    String tmp2 = String.valueOf(childDataSnapshot.getKey());
                    if (tmp2.equals("Block")){
                        BlockCitrusClass = tmp;
                    } else if (tmp2.equals("Title")){
                        TitleCitrusClass = tmp;
                    } else if (tmp2.equals("Url")){
                        UrlCitrusClass = tmp;
                    } else if (tmp2.equals("OldPrice")){
                        OldPriceCitrusClass = tmp;
                    } else if (tmp2.equals("Image")){
                        ImageCitrusClass = tmp;
                    } else if (tmp2.equals("LoadMore")){
                        LoadMoreCitrusClass = tmp;
                    } else if (tmp2.equals("Price")){
                        PriceCitrusClass = tmp;
                    }
                }
                Log.d("DATA", BlockCitrusClass);
                Log.d("DATA", TitleCitrusClass);
                Log.d("DATA", UrlCitrusClass);
                Log.d("DATA", OldPriceCitrusClass);
                Log.d("DATA", ImageCitrusClass);
                Log.d("DATA", LoadMoreCitrusClass);
                Log.d("DATA", PriceCitrusClass);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mDatabase.child("Class").child("PageProduct").child("Rozetka").addValueEventListener(new ValueEventListener() {
            public boolean inChosen;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();
                Log.d("DATA", "DATA3");

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                    Map<String,Object> taskMap = new HashMap<String,Object>();
                    objRef.updateChildren(taskMap);

                    String tmp = String.valueOf(childDataSnapshot.getValue());
                    String tmp2 = String.valueOf(childDataSnapshot.getKey());
                    if (tmp2.equals("Block")){
                        BlockRozetkaClass = tmp;
                    } else if (tmp2.equals("Title")){
                        TitleRozetkaClass = tmp;
                    } else if (tmp2.equals("Url")){
                        UrlRozetkaClass = tmp;
                    } else if (tmp2.equals("OldPrice")){
                        OldPriceRozetkaClass = tmp;
                    } else if (tmp2.equals("Image")){
                        ImageRozetkaClass = tmp;
                    } else if (tmp2.equals("LoadMore")){
                        LoadMoreRozetkaClass = tmp;
                    } else if (tmp2.equals("Price")){
                        PriceRozetkaClass = tmp;
                    }
                }
                Log.d("DATA", BlockRozetkaClass);
                Log.d("DATA", TitleRozetkaClass);
                Log.d("DATA", UrlRozetkaClass);
                Log.d("DATA", OldPriceRozetkaClass);
                Log.d("DATA", ImageRozetkaClass);
                Log.d("DATA", LoadMoreRozetkaClass);
                Log.d("DATA", PriceRozetkaClass);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        isFoundMoyo = false;
        isFoundCitrus = false;
        isFoundRozetka = false;
        browser = new WebView(getContext());
        browserMoyo = new WebView(getContext());
        browserRozetka = new WebView(getContext());
        currentStoreIndex = -1;

        //setContentView(R.layout.activity_main);
        initRecyclerView();


        listSearches = view.findViewById(R.id.searchHistory);
        listSearches.setVisibility(View.INVISIBLE);

        for (int i = 4; i >= 1; i--) {
            Log.d("SHAR", mSettings.getString(String.valueOf(i), ""));
            if (!mSettings.getString(String.valueOf(i), "").equals("")) {
                searches.add(mSettings.getString(String.valueOf(i), ""));
            }
        }

        adapterHistory = new HistoryAdapter(getContext(), searches);

        listSearches.setAdapter(adapterHistory);


        listSearches.setOnItemClickListener(new AdapterView.OnItemClickListener()  {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                String recentSearchString = searches.get(position);
                Log.d("TEST1", "onItemClick: ");
                mySearchView.setQuery(recentSearchString, false);
            }
        });












        errorTextMain = view.findViewById(R.id.errorTextMain);
        errorTextMain.setVisibility(View.INVISIBLE);
        mySearchView = (SearchView) view.findViewById(R.id.goodsSearchView);
        goodsListView = (ListView) view.findViewById(R.id.goodsList);
        storeListView = (RecyclerView) view.findViewById(R.id.storeListView);
        progressBar = view.findViewById(R.id.progressBar);
        adapterCitrus = new MyAdapter(getContext(), goodsNamesList, goodsPricesList, goodsOldPricesList, goodsImagesList, goodsColorsList, articleList);
        adapterMoyo = new MyAdapter(getContext(), goodsNamesListMoyo, goodsPricesListMoyo, goodsOldPricesListMoyo, goodsImagesListMoyo, goodsColorsMoyo, articleListMoyo);
        adapterRozetka = new MyAdapter(getContext(), goodsNamesListRozetka, goodsPricesListRozetka, goodsOldPricesListRozetka, goodsImagesListRozetka, goodsColorsRozetka, articleListRozetka);
        goodsListView.setAdapter(adapterMoyo);

        progressBar.setVisibility(View.GONE); // to hide

        getMoyo();
        getRozetka();

        storeTitlesList.add("Rozetka");
        storeTitlesList.add("Citrus");
        storeTitlesList.add("Allo");


        storeImagesList.add(R.drawable.rozetkalogo);
        storeImagesList.add(R.drawable.citruslogo);
        storeImagesList.add(R.drawable.allologo);

        storeColorsList.add(ContextCompat.getColor(context, R.color.rozetka_colour));
        storeColorsList.add(ContextCompat.getColor(context, R.color.citrus_colour));
        storeColorsList.add(ContextCompat.getColor(context, R.color.allo_colour));
        Log.d("THEME", String.valueOf(articleList.size()) + "- -");


        for (int i = goodsCount; i < articleList.size(); i++) {
            goodsNamesList.add(articleList.get(i).getName());
            goodsPricesList.add(articleList.get(i).getPrice());
            goodsOldPricesList.add(articleList.get(i).getOldPrice());
            goodsColorsList.add(ContextCompat.getColor(context, R.color.citrus_colour));

            goodsImagesList.add(articleList.get(i).getImg());

        }
        goodsCount = articleList.size();

        adapterCitrus.notifyDataSetChanged();
        goodsListView.setEnabled(true);


        storeListView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), storeListView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (isStoreLoaded) {
                            if (position == 0) {

                                currentStoreIndex = 0;
                                goodsListView.setAdapter(adapterMoyo);
                                Log.d("TEST", "Rozetka =>" + isLoadedRozetka);
                                if (isLoadedRozetka) {
                                    progressBar.setVisibility(View.GONE);

                                } else {
                                    progressBar.setVisibility(View.VISIBLE); // to hide
                                }

                            } else if (position == 1) {

                                currentStoreIndex = 1;
                                goodsListView.setAdapter(adapterCitrus);
                                Log.d("TEST", "Citrus =>" + isLoadedCitrus);
                                if (isLoadedCitrus) {
                                    progressBar.setVisibility(View.GONE);

                                } else {
                                    progressBar.setVisibility(View.VISIBLE); // to hide
                                }

                            } else if (position == 2) {

                                currentStoreIndex = 2;
                                goodsListView.setAdapter(adapterRozetka);
                                Log.d("TEST", "Allo =>" + isLoadedAllo);
                                if (isLoadedAllo) {
                                    progressBar.setVisibility(View.GONE);

                                } else {
                                    progressBar.setVisibility(View.VISIBLE); // to hide
                                }
                            }
                            initRecyclerView();
                        }
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
                            goodsOldPricesList.remove(goodsOldPricesList.size() - 1);
                            goodsNamesList.remove(goodsNamesList.size() - 1);
                            goodsImagesList.remove(goodsImagesList.size() - 1);
                            goodsColorsList.remove(goodsColorsList.size() - 1);
                            pageCounter++;
                            getCitrus(currentRequestString + "&page=" + pageCounter);


                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsOldPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsNamesListRozetka.clear();
                            goodsImagesListMoyo.clear();
                            goodsImagesListRozetka.clear();
                            goodsPricesListMoyo.clear();
                            goodsPricesListRozetka.clear();
                            goodsColorsList.clear();
                            goodsColorsMoyo.clear();
                            goodsColorsRozetka.clear();
                            goodsOldPricesListMoyo.clear();
                            goodsOldPricesListRozetka.clear();
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
                } else if (currentStoreIndex == 0) {
                   // Element loadElement = doc1.getElementsByAttributeValue("class", "show-more__text").first();


                    if (i != articleListMoyo.size() - 1) {
                        //Log.d(TAG, articleListMoyo.size() - 1 + "------" + i);
                        Log.d("TEST","goodclicked");
                        Uri address = Uri.parse(articleListMoyo.get(i).getUrl());
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);
                    } else {

                        if (isConnected()) {
                            articleListMoyo.remove(articleListMoyo.size() - 1);
                            goodsPricesListMoyo.remove(goodsPricesListMoyo.size() - 1);
                            goodsOldPricesListMoyo.remove(goodsOldPricesListMoyo.size() - 1);
                            goodsNamesListMoyo.remove(goodsNamesListMoyo.size() - 1);
                            goodsImagesListMoyo.remove(goodsImagesListMoyo.size() - 1);
                            goodsColorsMoyo.remove(goodsColorsMoyo.size() - 1);
                            pageCounterMoyo++;

                            getMoyoGoods("https://rozetka.com.ua/search/?page=" + pageCounterMoyo + "&text=" + currentRequestString);
                            Log.d("TEST","loadCLicked");



                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsOldPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsNamesListRozetka.clear();
                            goodsImagesListMoyo.clear();
                            goodsImagesListRozetka.clear();
                            goodsPricesListMoyo.clear();
                            goodsPricesListRozetka.clear();
                            goodsColorsList.clear();
                            goodsColorsMoyo.clear();
                            goodsColorsRozetka.clear();
                            goodsOldPricesListMoyo.clear();
                            goodsOldPricesListRozetka.clear();
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
                }else if (currentStoreIndex == 2) {
                    Element loadElement = doc2.getElementsByAttributeValue("class", "pagination__next__link").first();

                    if (loadElement == null ^ i != articleListRozetka.size() - 1) {
                        //Log.d(TAG, articleListRozetka.size() - 1 + "------" + i);
                        Uri address = Uri.parse(articleListRozetka.get(i).getUrl());
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);
                    } else {

                        if (isConnected()) {
                            articleListRozetka.remove(articleListRozetka.size() - 1);
                            goodsPricesListRozetka.remove(goodsPricesListRozetka.size() - 1);
                            goodsOldPricesListRozetka.remove(goodsOldPricesListRozetka.size() - 1);
                            goodsNamesListRozetka.remove(goodsNamesListRozetka.size() - 1);
                            goodsImagesListRozetka.remove(goodsImagesListRozetka.size() - 1);
                            goodsColorsRozetka.remove(goodsColorsRozetka.size() - 1);
                            pageCounterRozerka++;

                            getRozetkaGoods("https://allo.ua/ru/catalogsearch/result/index/p-"+pageCounterRozerka+"/?q="+currentRequestString.replace("%20", "+").replace("айфон","iphone"));



                        } else {

                            errorTextMain.setVisibility(View.VISIBLE);
                            goodsNamesList.clear();
                            goodsImagesList.clear();
                            goodsPricesList.clear();
                            goodsOldPricesList.clear();
                            goodsNamesListMoyo.clear();
                            goodsNamesListRozetka.clear();
                            goodsImagesListMoyo.clear();
                            goodsImagesListRozetka.clear();
                            goodsPricesListMoyo.clear();
                            goodsPricesListRozetka.clear();
                            goodsColorsList.clear();
                            goodsColorsMoyo.clear();
                            goodsColorsRozetka.clear();
                            goodsOldPricesListMoyo.clear();
                            goodsOldPricesListRozetka.clear();
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




        mySearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySearchView.setIconified(false);

                Log.d("FOCUS", "CLICK");
            }
        });


        mySearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View view, boolean hasFocus) {
                if (hasFocus) {
                    searches.clear();
                    for (int i = 4; i >= 1; i--) {
                        Log.d("SHAR", mSettings.getString(String.valueOf(i), ""));
                        if (!mSettings.getString(String.valueOf(i), "").equals("")) {
                            searches.add(mSettings.getString(String.valueOf(i), ""));
                        }
                    }

                    adapterHistory.notifyDataSetChanged();

                    Log.d("FOCUS", "FOCUSED");

                    listSearches.setVisibility(View.VISIBLE);
                    isFocusedSearch = false;

                } else {

                    Log.d("FOCUS", "UNFOCUSED");
                    listSearches.setVisibility(View.INVISIBLE);
                    isFocusedSearch = true;
                }
            }
        });

        // WORKS, DON'T CHANGE!!!

        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                stopHandlers = false;
                listSearches.setVisibility(View.INVISIBLE);
                currentStoreIndex = -1;
                progressBar.setVisibility(View.VISIBLE); // to hide
                isStoreLoaded = false;
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
                goodsOldPricesList.clear();
                goodsNamesListMoyo.clear();
                goodsImagesListMoyo.clear();
                goodsPricesListMoyo.clear();
                goodsOldPricesListMoyo.clear();
                goodsNamesListRozetka.clear();
                goodsImagesListRozetka.clear();
                goodsPricesListRozetka.clear();
                goodsOldPricesListRozetka.clear();
                goodsColorsList.clear();
                goodsColorsMoyo.clear();
                goodsColorsRozetka.clear();
                articleList.clear();
                articleListMoyo.clear();
                articleListRozetka.clear();
                if (isConnected()) {
                    errorTextMain.setVisibility(View.INVISIBLE);
                    currentRequestString = s;
                    adapterCitrus.notifyDataSetChanged();
                    adapterMoyo.notifyDataSetChanged();
                    adapterRozetka.notifyDataSetChanged();



                    getCitrus(s);
                    getMoyoGoods(s);
                    getRozetkaGoods("https://allo.ua/ru/catalogsearch/result/?q="+s.replace("айфон", "iphone"));

                    boolean isPut = false;

                    for (int i = 1; i <= 4; i++) {
                        if (mSettings.getString(String.valueOf(i), "").equals("") && s.equals(mSettings.getString(String.valueOf(i-1), ""))){
                            Log.d("SHAR1", mSettings.getString(String.valueOf(i-1), "")+" --- "+ s);
                            editor.putString(String.valueOf(i), s);
                            isPut = true;
                            editor.apply();
                            editor.commit();
                            break;
                        }
                    }

                    String tmp = mSettings.getString("4", "");

                    if (!isPut && !(s.equals(mSettings.getString("4", "")))){

                        editor.putString("4", s);
                        editor.apply();
                        editor.commit();

                        for (int i = 3; i >=1; i--) {
                            editor.putString(String.valueOf(i), tmp);
                            tmp = mSettings.getString(String.valueOf(i), "");

                            editor.apply();
                            editor.commit();
                        }

                    }

                    for (int i = 4; i >= 1; i--) {
                        Log.d("SHAR", mSettings.getString(String.valueOf(i), ""));
                    }

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
                stopHandlers = true;
                goodsNamesList.clear();
                goodsImagesList.clear();
                goodsPricesList.clear();
                goodsOldPricesList.clear();
                goodsNamesListMoyo.clear();
                goodsImagesListMoyo.clear();
                goodsPricesListMoyo.clear();
                goodsOldPricesListMoyo.clear();
                goodsNamesListRozetka.clear();
                goodsImagesListRozetka.clear();
                goodsPricesListRozetka.clear();
                goodsOldPricesListRozetka.clear();
                goodsColorsList.clear();
                goodsColorsMoyo.clear();
                goodsColorsRozetka.clear();
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

    return view;

    }



    private void initRecyclerView() {
        //Log.d(TAG, "initRecyclerView: init recyclerview");
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                    RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), storeTitlesList, storeImagesList, storeColorsList);
                    recyclerView.setAdapter(adapter);

                }
            });
        }

    }







    boolean isConnected() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else
            connected = false;

        return connected;
    }




    void getCitrus(String s){
        errorTextMain.setVisibility(View.INVISIBLE);

        runnable = new Runnable() {
            @Override
            public void run() {


                    try {
                        doc = Jsoup.connect("https://www.citrus.ua/search?query=" + s.replace(" ", "%20")).get();
                        Log.d("TEXT", doc.getElementsByAttributeValue("class", "product-img").attr("src"));
                        Log.d("CONNECT", "connected main Citrus");
                        Elements elements = doc.getElementsByAttributeValue("class", BlockCitrusClass);

                        elements.forEach(image -> {
                            if (!stopHandlers) {

                                try {

                                    Element aElement = image.getElementsByAttributeValue("class", UrlCitrusClass).last();
                                    Element priceElement2 = image.getElementsByAttributeValue("class", OldPriceCitrusClass).last();
                                    Element priceElement = image.getElementsByAttributeValue("class", PriceCitrusClass).last();
                                    String url = "https://www.citrus.ua" + aElement.attr("href");
                                    String title = image.getElementsByAttributeValue("class", TitleCitrusClass).last().text();
                                    String price = "";
                                    String oldPrice = "";

                                    if (priceElement != null) {
                                        String price1 = priceElement.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";

                                        if (priceElement2 != null) {
                                            String price2 = priceElement2.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";
                                            price = price1;
                                            oldPrice = price2;
                                        } else {
                                            price = price1;
                                            oldPrice = "";
                                        }

                                    } else {
                                        price = getString(R.string.form_price);
                                        oldPrice = "";
                                    }

                                    Document docCitrus = null;
                                    try {
                                        docCitrus = Jsoup.connect(url).timeout(0).get();
                                        Log.d("CONNECT", "connected next try" + stopHandlers);

                                        Elements images = docCitrus.getElementsByAttributeValue("class", ImageCitrusClass).select("img");
                                        String img = images.attr("src");

                                        articleList.add(new Article(url, title, price, oldPrice, img));
                                        if (currentStoreIndex == 1) {
                                            errorTextMain.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.d("CONNECT", "connected next except");

                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Rozetka Error");
                                    return;
                                }
                            } else {
                                return;
                            }
                        });

                        isLoadedCitrus = true;


                        for (int i = goodsCount; i < articleList.size(); i++) {
                            goodsNamesList.add(articleList.get(i).getName());
                            goodsPricesList.add(articleList.get(i).getPrice());
                            goodsOldPricesList.add(articleList.get(i).getOldPrice());
                            goodsColorsList.add(ContextCompat.getColor(context, R.color.citrus_colour));

                            goodsImagesList.add(articleList.get(i).getImg());

                        }
                        goodsCount = articleList.size();
                        Element loadElement = doc.getElementsByAttributeValue("class", LoadMoreCitrusClass).first();

                        if (loadElement != null && goodsCount > 0 && !stopHandlers) {
                            goodsNamesList.add(getString(R.string.load_more));
                            goodsPricesList.add("");
                            goodsOldPricesList.add("");
                            goodsColorsList.add(ContextCompat.getColor(context, R.color.loadmore_colour));

                            goodsImagesList.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                            articleList.add(new Article(" ", getString(R.string.load_more), "", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                        }
                        if (articleList.size() > 0) {
                            Log.d("TEST", "Citrus =>" + isLoadedCitrus);

                            if (!isStoreLoaded) {

                                currentStoreIndex = 1;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            goodsListView.setAdapter(adapterCitrus);
                                            initRecyclerView();
                                            progressBar.setVisibility(View.GONE); // to hide

                                        }

                                    });
                                }
                                isStoreLoaded = true;

                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // to hide
                                    adapterCitrus.notifyDataSetChanged();
                                    goodsListView.setEnabled(true);

                                    if (!isConnected()) {
                                        errorTextMain.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "CITRUS ERROR");
                    }


                }
        };


        webThread = new Thread(runnable);
        webThread.start();

    }



    void getMoyoGoods(String search) {


        goodsListView.setEnabled(false);
        //isFoundMoyo = false;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String search1 = "";
                search1 = search.replace(" ", "+");
                if (search.contains("page=")){
                    browserMoyo.loadUrl(search1);
                }else{
                    browserMoyo.loadUrl("https://rozetka.com.ua/search/?text=" + search1);
                }

            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    void getMoyo() {
        try {
//            isFoundMoyo = false;

            browserMoyo.setVisibility(View.INVISIBLE);
            browserMoyo.setLayerType(View.LAYER_TYPE_NONE, null);
            browserMoyo.getSettings().setJavaScriptEnabled(true);
            browserMoyo.getSettings().setBlockNetworkImage(false);
            browserMoyo.getSettings().setDomStorageEnabled(true);
            browserMoyo.getSettings().setLoadsImagesAutomatically(true);
            browserMoyo.getSettings().setBlockNetworkLoads(false);
            browserMoyo.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            browserMoyo.getSettings().setDefaultTextEncodingName("utf-8");

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
                                Log.d("CONNECT", "connected main Rozetka");
//                            if (!isFoundMoyo) {

                                doc1 = Jsoup.parse(htmlContent);
                                Elements elements = doc1.getElementsByAttributeValue("class", BlockRozetkaClass);

                                boolean isConsist = false;
                                for (Element element : elements) {
                                    if (!stopHandlers) {
                                        try {

                                            String url = element.getElementsByAttributeValue("class", UrlRozetkaClass).first().attr("href");
                                            Element elprice = element.getElementsByAttributeValue("class", PriceRozetkaClass).first();
                                                Element elprice2 = element.getElementsByAttributeValue("class", OldPriceRozetkaClass).first();

                                            String price = "";
                                            String oldPrice = "";

                                            if (elprice!=null) {

                                                if (elprice.text() != null) {
                                                    String price1 = elprice.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";

                                                    if (elprice2.text() != "") {
                                                        //Log.d("PRICE", elprice2.text());
                                                        String price2 = elprice2.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";
                                                        price = price1;
                                                        oldPrice = price2;
                                                    } else {
                                                        price = price1;
                                                        oldPrice = "";
                                                    }

                                                } else {
                                                    price = getString(R.string.form_price);
                                                    oldPrice = "";
                                                }
                                            } else {
                                                price = getString(R.string.form_price);
                                                oldPrice = "";
                                            }
                                            String title = element.getElementsByAttributeValue("class", TitleRozetkaClass).first().text();
                                            String imgUrl = element.getElementsByAttributeValue("class", ImageRozetkaClass).first().child(1).attr("src");

                                            boolean isContains = false;

                                            for (int i = 0; i < articleListMoyo.size(); i++) {
                                                if (articleListMoyo.get(i).getName().equals(title)) {
                                                    isContains = true;
                                                }
                                            }

                                            if (!isContains) {

                                                articleListMoyo.add(new Article(url, title, price, oldPrice, imgUrl));
                                                Log.d("TEST", new Article(url, title, price, oldPrice, imgUrl).toString());

                                            }


                                            isLoadedRozetka = true;

                                            if (currentStoreIndex == 0) {
                                                errorTextMain.setVisibility(View.INVISIBLE);
                                            }

                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            Log.d(TAG, "Rozetka Error");
                                            return;
                                        }
                                }
                                    else {
                                        return;
                                    }
                            }
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            for (int i = goodsCountMoyo; i < articleListMoyo.size(); i++) {
                                                goodsNamesListMoyo.add(articleListMoyo.get(i).getName());
                                                goodsPricesListMoyo.add(articleListMoyo.get(i).getPrice());
                                                goodsOldPricesListMoyo.add(articleListMoyo.get(i).getOldPrice());
                                                goodsImagesListMoyo.add(articleListMoyo.get(i).getImg());
                                                goodsColorsMoyo.add(ContextCompat.getColor(context, R.color.rozetka_colour));

                                            }
                                            goodsCountMoyo = articleListMoyo.size();
                                            Element loadElement = doc1.getElementsByAttributeValue("class", LoadMoreRozetkaClass).first();

                                            if (loadElement != null && goodsCount > 0 && !stopHandlers) {
                                                goodsNamesListMoyo.add(getString(R.string.load_more));
                                                goodsPricesListMoyo.add("");
                                                goodsOldPricesListMoyo.add("");
                                                goodsColorsMoyo.add(ContextCompat.getColor(context, R.color.loadmore_colour));

                                                goodsImagesListMoyo.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                                                articleListMoyo.add(new Article(" ", getString(R.string.load_more), "", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                                            }
                                            // to hide


                                        }
                                    });
                                }

                                if (articleListRozetka.size() > 0) {
                                    isLoadedRozetka = true;
                                    Log.d("TEST", "Rozetka =>" + isLoadedRozetka);

                                    if (!isStoreLoaded) {
                                        currentStoreIndex = 0;
                                        goodsListView.setAdapter(adapterMoyo);
                                        initRecyclerView();
                                        isStoreLoaded = true;
                                        progressBar.setVisibility(View.GONE); // to hide
                                    }

                                }


                            /*isLoadedRozetka = true;
                            Log.d("TEST", "Rozetka =>" + isLoadedRozetka);*/

                                //Log.d(TAG, articleListMoyo.size() + "  " + goodsImagesListMoyo.size());


                                isFoundMoyo = true;
                                adapterMoyo.notifyDataSetChanged();
                                goodsListView.setEnabled(true);
//                            }

                                //Log.d("TEST", doc.html());
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

                //if(getSupportActionBar()!=null) getSupportActionBar().setTitle(browserRozetka.getUrl());
                Log.d(TAG,"Step 2");
            }
        });

    }

    void getRozetka() {
        try {

            isFoundRozetka = false;
            Log.d(TAG,"Step 1");
            Log.d(TAG,"TEST");
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
                                Log.d("CONNECT", "connected main Allo");

                                if (!isFoundRozetka) {
                                    Log.d(TAG, "Step 3");
                                    //Log.d(TAG, htmlContent);
                                    doc2 = Jsoup.parse(htmlContent);
                                    Elements elements = doc2.getElementsByAttributeValue("class", BlockAlloClass);
                                    for (Element element : elements) {
                                        if (!stopHandlers) {
                                            try {

                                                String title = element.getElementsByAttributeValue("class", TitleAlloClass).first().text();
                                                String url = element.getElementsByAttributeValue("class", UrlAlloClass).first().attr("href");
                                                Element el = element.getElementsByAttributeValue("class", PriceAlloClass).last();
                                                Element el2 = element.getElementsByAttributeValue("class", OldPriceAlloClass).first();
                                                String price;
                                                String oldPrice;
                                                if (el != null) {
                                                    String price1 = el.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";


                                                    if (el2 != null && !el2.equals(el)) {
                                                        String price2 = el2.text().replace("₴", "").replace(" грн", "").replace("грн", "") + " грн";
                                                        price = price1;
                                                        oldPrice = price2;
                                                    } else {
                                                        price = price1;
                                                        oldPrice = "";
                                                    }

                                                } else {
                                                    price = getString(R.string.form_price);
                                                    oldPrice = "";
                                                }
                                                // String imgUrl = String.valueOf(element.getElementsByAttributeValue("class", "goods-tile__picture").first().childrenSize());
                                                String imgUrl = element.getElementsByAttributeValue("class", ImageAlloClass).first().select("img").attr("data-src");

                                                articleListRozetka.add(new Article(url, title, price, oldPrice, imgUrl));

                                                isLoadedAllo = true;

                                                if (currentStoreIndex == 2) {
                                                    errorTextMain.setVisibility(View.INVISIBLE);
                                                }

                                            }catch (Exception e) {
                                                e.printStackTrace();
                                                Log.d(TAG, "Rozetka Error");
                                                return;
                                            }
                                    } else {
                                            return;
                                        }
                                }
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                for (int i = goodsCountRozetka; i < articleListRozetka.size(); i++) {
                                                    goodsNamesListRozetka.add(articleListRozetka.get(i).getName());
                                                    goodsPricesListRozetka.add(articleListRozetka.get(i).getPrice());
                                                    goodsOldPricesListRozetka.add(articleListRozetka.get(i).getOldPrice());
                                                    goodsColorsRozetka.add(ContextCompat.getColor(context, R.color.allo_colour));

                                                    goodsImagesListRozetka.add(articleListRozetka.get(i).getImg());
                                                    //Log.d(TAG, articleListRozetka.get(i).toString());

                                                }
                                                goodsCountRozetka = articleListRozetka.size();
                                                Element loadElement = doc2.getElementsByAttributeValue("class", LoadMoreAlloClass).last();

                                                if (loadElement != null && goodsCount > 0 && !stopHandlers) {
                                                    goodsNamesListRozetka.add(getString(R.string.load_more));
                                                    goodsPricesListRozetka.add("");
                                                    goodsOldPricesListRozetka.add("");
                                                    goodsColorsRozetka.add(ContextCompat.getColor(context, R.color.loadmore_colour));

                                                    goodsImagesListRozetka.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                                                    articleListRozetka.add(new Article(" ", getString(R.string.load_more), "", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                                                }


                                            }
                                        });
                                    }


                                    if (articleListRozetka.size() > 0) {
                                        isLoadedAllo = true;
                                        Log.d("TEST", "Allo =>" + isLoadedAllo);
                                        if (!isStoreLoaded) {
                                            currentStoreIndex = 2;
                                            goodsListView.setAdapter(adapterRozetka);
                                            initRecyclerView();
                                            isStoreLoaded = true;
                                            progressBar.setVisibility(View.GONE); // to hide
                                        }
                                  /*  if (!isLoadedCitrus && !isLoadedRozetka && isLoadedAllo) {
                                        Log.d("TEST", "Allo => SHOW");


                                        // isLoadedAllo = false;
                                        initRecyclerView();
                                    }*/
                                    }


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
        List<String> rDescription2 = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<Integer> rColor = new ArrayList();
        List<Article> arrLIst = new ArrayList<>();
        boolean isGoodsChoosen = false;


        MyAdapter(Context c, List<String> title, List<String> description, List<String> description2, List<String> imgs,List<Integer> color, List arrList) {
            super(c, R.layout.goods_view, R.id.goodsTitleMain, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rDescription2 = description2;
            this.rImgs = imgs;
            this.rColor = color;
            this.arrLIst = arrList;

        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View goodsView = layoutInflater.inflate(R.layout.goods_view, parent, false);
            ImageView images = goodsView.findViewById(R.id.goodsImageMain);
            ImageView imageFavourite = goodsView.findViewById(R.id.goodsFavourite);
            TextView myTitle = goodsView.findViewById(R.id.goodsTitleMain);
            TextView myDescription = goodsView.findViewById(R.id.goodsPriceMain);
            TextView myDescription2 = goodsView.findViewById(R.id.oldGoodsPriceMain);
            CardView backgroundCard = goodsView.findViewById(R.id.backgroungCard);


            myDescription.setTextColor(ContextCompat.getColor(context,R.color.price_color));
            myDescription2.setTextColor(ContextCompat.getColor(context,R.color.oldprice_colour));
            // now set our resources on views


            Glide.with(context).load(rImgs.get(position)).into(images);
            myTitle.setText(rTitle.get(position));
            backgroundCard.setCardBackgroundColor(rColor.get(position));


            mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                public boolean inChosen;
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {



                    try {

                        for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                            String str = arrLIst.get(position).getUrl();
                            String[] goodsInfo = String.valueOf(childDataSnapshot.getValue()).split("SPLITFORBUY", 2);
                            if (goodsInfo[0].equals(str)){

                                imageFavourite.setImageResource(R.drawable.ic_baseline_star_24);
                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



            if (rTitle.get(position) == getString(R.string.load_more)) {
                imageFavourite.setVisibility(View.GONE);
                myTitle.setTypeface(null, Typeface.BOLD_ITALIC);
                myTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }
            try {
                myDescription.setText(rDescription.get(position));
                myDescription2.setText(rDescription2.get(position));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Rozetka Error");
            }






            imageFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.d(TAG,arrLIst.get(position).getUrl());
                    //Log.d(TAG,arrLIst.get(position).getImg());
                    //Log.d(TAG,arrLIst.get(position).getName());
                    // Log.d(TAG,arrLIst.get(position).getPrice());


                    if (isConnected()){
                        mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                            public boolean inChosen;
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();

                                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                                    Map<String,Object> taskMap = new HashMap<String,Object>();
                                    objRef.updateChildren(taskMap);


                                    String str = arrLIst.get(position).getUrl();
                                    String[] goodsInfo = String.valueOf(childDataSnapshot.getValue()).split("SPLITFORBUY", 2);
                                    if (goodsInfo[0].equals(str)){
                                        inChosen = true;

                                        imageFavourite.setImageResource(R.drawable.ic_baseline_star_border_24);

                                        mDatabase.child("Users").child(user.getUid()).child(childDataSnapshot.getKey()).setValue(null);
                                        mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                                        //imageFavourite.setImageResource(R.drawable.ic_baseline_star_24);

                                        break;

                                    }else{
                                        inChosen = false;
                                        imageFavourite.setImageResource(R.drawable.ic_baseline_star_24);

                                    }


                                }


                                if (!inChosen){
                                    imageFavourite.setImageResource(R.drawable.ic_baseline_star_24);
                                    String str = "";
                                    if (arrLIst.get(position).getUrl().contains("allo.ua")){
                                        str = "ALLO";
                                    } else if (arrLIst.get(position).getUrl().contains("citrus.ua")){
                                        str = "CITRUS";
                                    } else if (arrLIst.get(position).getUrl().contains("rozetka.com.ua")){
                                        str = "ROZETKA";
                                    }
                                    mDatabase.child("Users").child(user.getUid()).push().setValue(arrLIst.get(position).getUrl()+"SPLITFORBUY"+arrLIst.get(position).getImg()+"SPLITFORBUY"+arrLIst.get(position).getName()+"SPLITFORBUY"+str);
                                    mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                    return;
                                }




                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        Toast.makeText(getActivity(),getString(R.string.internet_error),Toast.LENGTH_SHORT).show();
                    }
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
        private String oldPrice;


        public Article(String url, String name, String price, String oldPrice, String img) {
            this.url = url;
            this.name = name;
            this.price = price;
            this.oldPrice = oldPrice;
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

        public String getOldPrice() { return oldPrice; }

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
                    ", oldPrice='" + price + '\'' +
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
        private ArrayList<Integer> mImageUrls = new ArrayList<>();
        private ArrayList<Integer> mCardColors = new ArrayList<>();
        private Context mContext;

        public RecyclerViewAdapter(Context context, ArrayList<String> names, ArrayList<Integer> imageUrls, ArrayList<Integer> cardColors) {
            mNames = names;
            mImageUrls = imageUrls;
            mContext = context;
            mCardColors = cardColors;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            //Log.d(TAG, "onBindViewHolder: called.");


            if (currentStoreIndex==0 && isLoadedRozetka){
                errorTextMain.setVisibility(View.INVISIBLE);
            } else if (currentStoreIndex==1 && isLoadedCitrus) {
                errorTextMain.setVisibility(View.INVISIBLE);
            } else if (currentStoreIndex==2 && isLoadedAllo){
                errorTextMain.setVisibility(View.INVISIBLE);
            } else if (currentStoreIndex==0 && !isConnected() && !isLoadedRozetka){
                errorTextMain.setVisibility(View.VISIBLE);
            } else if (currentStoreIndex==1 && !isConnected() && !isLoadedCitrus) {
                errorTextMain.setVisibility(View.VISIBLE);
            } else if (currentStoreIndex==2 && !isConnected() && !isLoadedAllo){
                errorTextMain.setVisibility(View.VISIBLE);
            } else if (currentStoreIndex==0 && isConnected() && !isLoadedRozetka){
                errorTextMain.setVisibility(View.INVISIBLE);
            } else if (currentStoreIndex==1 && isConnected() && !isLoadedCitrus) {
                errorTextMain.setVisibility(View.INVISIBLE);
            } else if (currentStoreIndex==2 && isConnected() && !isLoadedAllo){
                errorTextMain.setVisibility(View.INVISIBLE);
            }





            holder.image.setImageResource(mImageUrls.get(position));

            holder.name.setText(mNames.get(position));
            holder.card.setCardBackgroundColor(mCardColors.get(position));

            if(position == currentStoreIndex){
                holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

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
            CardView card;

            public ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.storeImageMain);
                name = itemView.findViewById(R.id.storeTitleMain);
                card = itemView.findViewById(R.id.storeCard);
            }
        }
    }


    class HistoryAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> historyText = new ArrayList();




        HistoryAdapter(Context c, List<String> searches) {
            super(c, R.layout.favourite_view, R.id.historySearchString, searches);
            this.context = c;
            this.historyText = searches;


        }



        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View goodsView = layoutInflater.inflate(R.layout.favourite_view, parent, false);
            TextView history = goodsView.findViewById(R.id.historySearchString);
            history.setTextColor(ContextCompat.getColor(context,R.color.text_searches));
            ImageView search = goodsView.findViewById(R.id.historySearch);
            ImageView historyIcon = goodsView.findViewById(R.id.historyIcon);


            history.setText(historyText.get(position));
            search.setImageResource(R.drawable.ic_baseline_search_24);
            historyIcon.setImageResource(R.drawable.ic_baseline_history_24);

            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String recentSearchString = searches.get(position);
                    Log.d("TEST1", "onItemClick: ");
                    mySearchView.setQuery(recentSearchString, true);

                }
            });


            return goodsView;
        }}}
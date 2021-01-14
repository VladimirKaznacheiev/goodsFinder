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
    int currentStoreIndex;
    SearchView mySearchView;
    ListView goodsListView;
    RecyclerView storeListView;
    ProgressBar progressBar;
    int pageCounter = 1;
    int pageCounterMoyo = 1;
    int goodsCount = 0;
    int goodsCountMoyo = 0;
    List<Article> articleList = new ArrayList<>();
    List<Article> articleListMoyo = new ArrayList<>();
    ArrayList<String> goodsNamesList = new ArrayList<>();
    ArrayList<String> goodsPricesList = new ArrayList<>();
    ArrayList<String> goodsImagesList = new ArrayList<>();

    ArrayList<String> goodsImagesListMoyo = new ArrayList<>();
    ArrayList<String> goodsPricesListMoyo = new ArrayList<>();
    ArrayList<String> goodsNamesListMoyo = new ArrayList<>();


    ArrayList<String> storeTitlesList = new ArrayList<>();
    ArrayList<String> storeImagesList = new ArrayList<>();
    String currentRequestString = "";
    TextView errorTextMain;

    private Document doc = null;
    private Document doc1 = null;

    private Thread webThread;
    private Runnable runnable;
    MyAdapter adapterCitrus;
    MyAdapter adapterMoyo;

    WebView browser;
    WebView browserMoyo;

    public int getCurrentStoreIndex() {
        return currentStoreIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isFoundMoyo = false;
        isFoundCitrus = false;
        browser = new WebView(this);
        browserMoyo = new WebView(this);
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
        adapterCitrus = new MyAdapter(this, goodsNamesList, goodsPricesList, goodsImagesList);
        adapterMoyo = new MyAdapter(this, goodsNamesListMoyo, goodsPricesListMoyo, goodsImagesListMoyo);
        goodsListView.setAdapter(adapterMoyo);


        getMoyo();
        getCitrus();

        storeTitlesList.add("MoYo");
        storeTitlesList.add("Citrus");


        storeImagesList.add("https://img.moyo.ua/img/logos/2020/LOGO_site-min_1608538727.jpg");
        storeImagesList.add("https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");


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
                            getCitrusGoods(currentRequestString + "&page=" + pageCounter);

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
                goodsCountMoyo = 0;
                goodsCount = 0;
                goodsNamesList.clear();
                goodsImagesList.clear();
                goodsPricesList.clear();
                goodsNamesListMoyo.clear();
                goodsImagesListMoyo.clear();
                goodsPricesListMoyo.clear();
                articleList.clear();
                articleListMoyo.clear();
                if (isConnected()) {
                    errorTextMain.setVisibility(View.INVISIBLE);
                    currentRequestString = s;
                    adapterCitrus.notifyDataSetChanged();
                    adapterMoyo.notifyDataSetChanged();

                    progressBar.setVisibility(View.VISIBLE); //to show


                    getMoyoGoods(s);
                    getCitrusGoods(s);

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
                articleList.clear();
                articleListMoyo.clear();
                adapterCitrus.notifyDataSetChanged();
                adapterMoyo.notifyDataSetChanged();
                currentRequestString = "";
                pageCounter = 1;
                pageCounterMoyo = 1;
                goodsCount = 0;
                goodsCountMoyo = 0;
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



    void getCitrusGoods(String search) {
        goodsListView.setEnabled(false);
        isFoundCitrus = false;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String search1 = search.replace(" ", "+");
                browser.loadUrl("https://www.citrus.ua/search?query=" + search1);

            }
        });
    }

    void getCitrus(){
        //citrus
        try {
            isFoundCitrus = false;
            browser.setWebChromeClient(new WebChromeClient());
            browser.setVisibility(View.INVISIBLE);
            browser.setLayerType(View.LAYER_TYPE_NONE, null);
            browser.getSettings().setJavaScriptEnabled(true);
            browser.getSettings().setBlockNetworkImage(false);
            browser.getSettings().setDomStorageEnabled(true);
            browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            browser.getSettings().setLoadsImagesAutomatically(true);
            browser.getSettings().setGeolocationEnabled(false);
            browser.getSettings().setSupportZoom(false);
            browser.getSettings().setBlockNetworkLoads(false);
            browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            browser.addJavascriptInterface(new JSHtmlInterface_citrus(), "JSBridge");

            browser.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {

                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            browser.loadUrl("javascript:window.JSBridge.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Citrus Error");
        }


    }

    private class JSHtmlInterface_citrus {
        @android.webkit.JavascriptInterface
        public void showHTML(String html) {
            final String htmlContent = html;

            uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (!isFoundCitrus) {
                                doc = Jsoup.parse(htmlContent);
                                Elements elements = doc.getElementsByAttributeValue("class", "short-itm-desc");
                                for (Element element : elements) {
                                    Element priceElement = element.child(7);
                                    String title = element.child(5).child(0).text();
                                    String url = "https://www.citrus.ua" + element.child(5).attr("href");
                                    String price = "";
                                    if (priceElement.child(1).childrenSize() > 1) {
                                        if (priceElement.child(1).child(1).attr("class") == "price-not-ready") {
                                            price = "Цена формируется";
                                        } else {
                                            price = priceElement.child(1).child(1).child(0).text();
                                        }
                                    } else {
                                        if (priceElement.child(1).child(0).attr("class") == "price-not-ready") {
                                            price = "Цена формируется";
                                        } else {
                                            price = priceElement.child(1).child(0).text();
                                        }
                                    }
                                    String imgUrl = element.child(2).child(1).child(0).attr("data-src");

                                    articleList.add(new Article(url, title, price, imgUrl));
                                }

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Log.d(TAG, "MoyoGoodsList");
                                        for (int i = goodsCount; i < articleList.size(); i++) {
                                            goodsNamesList.add(articleList.get(i).getName());
                                            goodsPricesList.add(articleList.get(i).getPrice() + " грн");

                                            goodsImagesList.add(articleList.get(i).getImg());

                                            Log.d(TAG,articleList.get(i).toString());

                                        }
                                        goodsCount = articleList.size();
                                        Element loadElement = doc.getElementsByAttributeValue("class", "catalog-card-container more-items product-card product-card--mini").first();

                                        if (loadElement != null) {
                                            goodsNamesList.add("Загрузить ещё");
                                            goodsPricesList.add("");

                                            goodsImagesList.add("https://image.flaticon.com/icons/png/512/16/16770.png");
                                            articleList.add(new Article(" ", "Загрузить ещё", "", "https://image.flaticon.com/icons/png/512/16/16770.png"));

                                        }
                                        progressBar.setVisibility(View.GONE); // to hide


                                    }
                                });

                                Log.d(TAG, articleList.size() + "  " + goodsImagesList.size());

                                isFoundCitrus = true;
                                adapterCitrus.notifyDataSetChanged();
                                goodsListView.setEnabled(true);
                            }
                        
                    }}
            );
        }
    }







    void getMoyoGoods(String search) {
        goodsListView.setEnabled(false);
        isFoundMoyo = false;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Step 2");
                String search1 = search.replace(" ", "+");
                browserMoyo.loadUrl("https://www.moyo.ua/search/new/?q=" + search1);

            }
        });

    }

    void getMoyo() {
        try {
            isFoundMoyo = false;
            Log.d(TAG,"Step 1");
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
                                Log.d(TAG,"Step 3");
                                doc1 = Jsoup.parse(htmlContent);
                                Elements elements = doc1.getElementsByAttributeValue("class", "product-tile_inner");
                                Log.d(TAG, "MoyoSearch");
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
                                        Log.d(TAG, "MoyoGoodsList");
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


    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rImgs = new ArrayList();


        MyAdapter(Context c, List<String> title, List<String> description, List<String> imgs) {
            super(c, R.layout.goods_view, R.id.goodsTitleMain, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rImgs = imgs;

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
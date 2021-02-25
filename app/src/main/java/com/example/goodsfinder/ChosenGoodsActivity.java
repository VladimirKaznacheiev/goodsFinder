package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChosenGoodsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private List<String> DiscrTasks;

    private Document doc = null;
    private Document doc1 = null;
    private final Handler uiHandler = new Handler();

    private String priceRozetka = "0";
    private int counterRozetka = 0;
    private int iRozetka = 0;
    private boolean isFor = false;

    boolean isLoadedRozetka = false;
    private  boolean isShowed = false;

    private List<String> urlsRozetka = new ArrayList<String>();
    private List<String> positionsRozetka = new ArrayList<String>();

    private List<String> infoLoaded = new ArrayList<String>();
    private List<String> infoLoadedUp = new ArrayList<String>();

    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    WebView browserRozetka;

    ArrayList<String> favouritesImagesList = new ArrayList<>();
    ArrayList<String> favouritesPricesList = new ArrayList<>();
    ArrayList<String> favouritesNamesList = new ArrayList<>();
    ArrayList<String> favouritesUrlList = new ArrayList<>();
    ArrayList<String> favouritesList = new ArrayList<>();

    ListView goodsListView;
    MyAdapter adapter1;


    FirebaseListAdapter mAdapter;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chosen_goods_activity);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        browserRozetka = new WebView(this);
        getMoyo();
        findViewById(R.id.btn_to_main).setOnClickListener(this::onClick);
        findViewById(R.id.btn_sort_up).setOnClickListener(this::onClick);
        findViewById(R.id.btn_sort_down).setOnClickListener(this::onClick);
        findViewById(R.id.btn_sort_default).setOnClickListener(this::onClick);

        goodsListView = (ListView) findViewById(R.id.favouritesView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(ChosenGoodsActivity.this, MainActivity.class);
            startActivity(intent);
        }

        if(getSupportActionBar()!=null) getSupportActionBar().setTitle(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        adapter1 = new MyAdapter(this,favouritesNamesList,favouritesPricesList,favouritesImagesList,favouritesUrlList);
        goodsListView.setAdapter(adapter1);

        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Uri address = Uri.parse(favouritesUrlList.get(i));
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);
            }

        });

            mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    Map<String, Object> td = (HashMap<String, Object>) snapshot.getValue();
                    favouritesUrlList.clear();
                    favouritesImagesList.clear();
                    favouritesNamesList.clear();
                    favouritesPricesList.clear();

                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                        int counter = 0;
                        DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child(childDataSnapshot.getKey());
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        objRef.updateChildren(taskMap);


                        favouritesList.add(String.valueOf(childDataSnapshot.getValue()));
                        String[] goodsInfo = String.valueOf(childDataSnapshot.getValue()).split("split", 4);


                        favouritesUrlList.add(counter, goodsInfo[0]);
                        favouritesImagesList.add(counter, goodsInfo[1]);
                        favouritesNamesList.add(counter, goodsInfo[2]+"\n"+goodsInfo[3]);

                        if (goodsInfo[0].contains("allo.ua")) {
                            try {

                                mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                for (int i = 0; i < positionsRozetka.size(); i++) {
                                    positionsRozetka.set(i, String.valueOf(Integer.parseInt(positionsRozetka.get(i)) + 1));
                                }

                                doc = Jsoup.connect(goodsInfo[0]).get();
                                Element elements = doc.getElementsByAttributeValue("class", "sum").last();
                                Element elements2 = doc.getElementsByAttributeValue("class", "sum").first();

                                if (elements!=null){
                                    String price1 = elements.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";

                                    if (elements2!=null && !elements2.equals(elements)){
                                        String price2 = elements2.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";
                                        favouritesPricesList.add(counter,  "АКЦИЯ\nЗачёркнутая цена:\n" + price2 + "\nСейчас цена:\n" +  price1);
                                        infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT"+price2+"SPLIT"+price1);
                                    } else {
                                        favouritesPricesList.add(counter,  price1);
                                        infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT"+"0"+"SPLIT"+price1);
                                    }



                                } else {
                                    favouritesPricesList.add(counter, "Ціна формується");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (goodsInfo[0].contains("rozetka.com.ua")) {
                            mDatabase.child("Users").child(user.getUid()).removeEventListener(this);


                            for (int i = 0; i < positionsRozetka.size(); i++) {
                                positionsRozetka.set(i, String.valueOf(Integer.parseInt(positionsRozetka.get(i)) + 1));
                            }
                            urlsRozetka.add(goodsInfo[0]);
                            positionsRozetka.add(String.valueOf(counter));

                            favouritesPricesList.add(counter,  "Loading...");
                            infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT");

                        } else if (goodsInfo[0].contains("citrus.ua")) {

                            try {

                                mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                for (int i = 0; i < positionsRozetka.size(); i++) {
                                    positionsRozetka.set(i, String.valueOf(Integer.parseInt(positionsRozetka.get(i)) + 1));
                                }

                                doc = Jsoup.connect(goodsInfo[0]).get();
                                Element elements = doc.getElementsByAttributeValue("class", "buy-section__new-price").last();
                                Element elements2 = doc.getElementsByAttributeValue("class", "buy-section__old-price").last();

/*                                String price1 = elements.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";
                                String price2 = elements2.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";*/

                                if (elements!=null){
                                    String price1 = elements.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";

                                    if (elements2!=null){
                                        String price2 = elements2.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";
                                        favouritesPricesList.add(counter,  "АКЦИЯ\nЗачёркнутая цена:\n" + price2 + "\nСейчас цена:\n" +  price1);
                                        infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT"+price2+"SPLIT"+price1);
                                    } else {
                                        favouritesPricesList.add(counter,  price1);
                                        infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT"+"0"+"SPLIT"+price1);
                                    }

                                } else {
                                    favouritesPricesList.add(counter, "Ціна формується");
                                    infoLoaded.add(goodsInfo[0]+"SPLIT"+goodsInfo[1]+"SPLIT"+goodsInfo[2]+"SPLIT"+goodsInfo[3]+"SPLIT"+"0"+"SPLIT"+"0");
                                }



                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter1.notifyDataSetChanged();
                        counter++;

                    }

                    //Log.d("TEST", String.valueOf(urlsRozetka));
                    //Log.d("TEST", String.valueOf(positionsRozetka));


                    for (int i = 0; i < infoLoaded.size(); i++) {
                        infoLoadedUp.add(i, infoLoaded.get(i));
                    }

                    iRozetka = 0;

                    try {
                        browserRozetka.loadUrl(urlsRozetka.get(urlsRozetka.size() - 1));
                    } catch (Exception e) {
                        isLoadedRozetka = true;
                        e.printStackTrace();
                        Log.d("ERROR", "Load Error");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




    }

    void getMoyo() {
        try {
            browserRozetka.setVisibility(View.INVISIBLE);
            browserRozetka.setLayerType(View.LAYER_TYPE_NONE, null);
            browserRozetka.getSettings().setJavaScriptEnabled(true);
            browserRozetka.getSettings().setBlockNetworkImage(false);
            browserRozetka.getSettings().setDomStorageEnabled(true);
            browserRozetka.getSettings().setLoadsImagesAutomatically(true);
            browserRozetka.getSettings().setBlockNetworkLoads(false);
            browserRozetka.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            browserRozetka.getSettings().setDefaultTextEncodingName("utf-8");
            //browserRozetka.getSettings().setLoadWithOverviewMode(true);

            browserRozetka.addJavascriptInterface(new JSHtmlInterfaceMoyo(), "JSBridge");

            browserRozetka.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            browserRozetka.loadUrl("javascript:window.JSBridge.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                            //Log.d("TEST1", "loaded");
                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROR", "Rozetka Error");
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

                try {
                    doc1 = Jsoup.parse(htmlContent);
                    priceRozetka = doc1.getElementsByAttributeValue("class", "product-prices__inner").last().child(0).text();
                    Element oldPriceRozetka = doc1.getElementsByAttributeValue("class", "product-prices__small").last();

                    if (priceRozetka!=null){
                        String price1 = priceRozetka.replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";

                        if (oldPriceRozetka!=null){
                            String price2 = oldPriceRozetka.text().replace("₴", "").replace("грн", "").replace(" грн", "") + " грн";
                            favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  "АКЦИЯ\nЗачёркнутая цена:\n" + price2 + "\nСейчас цена:\n" +  price1);
                            infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+price2+"SPLIT"+price1);
                        } else {
                            favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  price1);
                            infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+"0"+"SPLIT"+price1);
                        }




                    } else {
                        favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), "Ціна формується");
                    }

                    //favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), priceRozetka.replace("₴", "").replace("грн", "").replace(" грн", "") + " грн");
                    adapter1.notifyDataSetChanged();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(iRozetka>=urlsRozetka.size()-1){

                                for (int i = 0; i < infoLoaded.size(); i++) {
                                    infoLoadedUp.set(i, infoLoaded.get(i));
                                }

                                isLoadedRozetka = true;

                                return;
                            }

                            iRozetka ++;
                            browserRozetka.loadUrl(urlsRozetka.get(urlsRozetka.size()-1-iRozetka));
                    }
                });


                            //favouritesPricesList.add(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), priceRozetka.replace("₴", ""));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ERROR", "Moyo Error");
                    priceRozetka = "0";
                }
                        }
                    }
            );
        }
    }

    public void onClick(View view) {
        if(view.getId() == R.id.btn_to_main) {
            Intent intent = new Intent(ChosenGoodsActivity.this, MainActivity.class);
            startActivity(intent);

        }

        if(view.getId() == R.id.btn_sort_up) {
            if (isLoadedRozetka){
                Log.d("BEFORE", String.valueOf(infoLoaded));
            boolean sorted = false;
            String temp;
            while(!sorted) {
                sorted = true;
                for (int i = 0; i < infoLoadedUp.size() - 1; i++) {

                    String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLIT", 6);
                    String[] goodsInfoNext = String.valueOf(infoLoadedUp.get(i+1)).split("SPLIT", 6);

                    Log.d("START", String.valueOf(goodsInfo[2])+", "+goodsInfoNext[2]);
                    Log.d("FIRST", String.valueOf(goodsInfo[4])+", "+goodsInfoNext[4]);
                    Log.d("SECOND", String.valueOf(goodsInfo[5])+", "+goodsInfoNext[5]);

                    if (Integer.parseInt(goodsInfo[5].replace("грн", "").replace(" ", "")) > Integer.parseInt(goodsInfoNext[5].replace("грн", "").replace(" ", ""))) {
                        temp = infoLoadedUp.get(i);

                        infoLoadedUp.set(i, infoLoadedUp.get(i+1));

                        infoLoadedUp.set(i+1, temp);
                        sorted = false;
                    }
                }
            }


            favouritesUrlList.clear();
            favouritesImagesList.clear();
            favouritesNamesList.clear();
            favouritesPricesList.clear();

            for (int i = 0; i < infoLoadedUp.size(); i++) {
                String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLIT", 6);
                favouritesUrlList.add(i, goodsInfo[0]);
                favouritesImagesList.add(i, goodsInfo[1]);
                favouritesNamesList.add(i, goodsInfo[2]+"\n"+goodsInfo[3]);
                favouritesPricesList.add(i, infoLoadedUp.get(i));
                if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                    if (Integer.parseInt(goodsInfo[4].replace(" ", "").replace("грн", "")) != 0) {
                        favouritesPricesList.add(i, "АКЦИЯ\nЗачёркнутая цена:\n" + goodsInfo[4] + "\nСейчас цена:\n" + goodsInfo[5]);
                    } else {
                        favouritesPricesList.add(i, goodsInfo[5]);
                    }
                } else {
                    favouritesPricesList.add(i, "Ціна формується");
                }


            }
            adapter1.notifyDataSetChanged();



        }else{
                Toast.makeText(ChosenGoodsActivity.this, "Загрузились не все товары, подождите", Toast.LENGTH_SHORT).show();
            }
        }

        if(view.getId() == R.id.btn_sort_down) {
            if (isLoadedRozetka){
                Log.d("TEST", String.valueOf(infoLoaded));
            boolean sorted = false;
            String temp;
            while(!sorted) {
                sorted = true;
                for (int i = 0; i < infoLoadedUp.size() - 1; i++) {

                    String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLIT", 6);
                    String[] goodsInfoNext = String.valueOf(infoLoadedUp.get(i+1)).split("SPLIT", 6);

                    if (Integer.parseInt(goodsInfo[5].replace("грн", "").replace(" ", "")) < Integer.parseInt(goodsInfoNext[5].replace("грн", "").replace(" ", ""))) {
                        temp = infoLoadedUp.get(i);
                        infoLoadedUp.set(i, infoLoadedUp.get(i+1));
                        infoLoadedUp.set(i+1, temp);
                        sorted = false;
                    }
                }
            }

                Log.d("TEST", String.valueOf(infoLoaded));

            favouritesUrlList.clear();
            favouritesImagesList.clear();
            favouritesNamesList.clear();
            favouritesPricesList.clear();

            for (int i = 0; i < infoLoadedUp.size(); i++) {
                String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLIT", 6);
                favouritesUrlList.add(i, goodsInfo[0]);
                favouritesImagesList.add(i, goodsInfo[1]);
                favouritesNamesList.add(i, goodsInfo[2]+"\n"+goodsInfo[3]);
                favouritesPricesList.add(i, infoLoadedUp.get(i));

                if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                    if (Integer.parseInt(goodsInfo[4].replace(" ", ""). replace("грн", "")) != 0){
                        favouritesPricesList.add(i,  "АКЦИЯ\nЗачёркнутая цена:\n" + goodsInfo[4] + "\nСейчас цена:\n" +  goodsInfo[5]);
                    } else{
                        favouritesPricesList.add(i,  goodsInfo[5]);
                    }
                }else {
                    favouritesPricesList.add(i, "Ціна формується");
                }

            }
            adapter1.notifyDataSetChanged();



        }else{
                Toast.makeText(ChosenGoodsActivity.this, "Загрузились не все товары, подождите", Toast.LENGTH_SHORT).show();
            }
        }

        if(view.getId() == R.id.btn_sort_default) {
            if (isLoadedRozetka){

                Log.d("COMMON", String.valueOf(infoLoaded));

                favouritesUrlList.clear();
                favouritesImagesList.clear();
                favouritesNamesList.clear();
                favouritesPricesList.clear();

                for (int i = 0; i < infoLoaded.size(); i++) {
                    String[] goodsInfo = String.valueOf(infoLoaded.get(infoLoaded.size()-1-i)).split("SPLIT", 6);
                    favouritesUrlList.add(i, goodsInfo[0]);
                    favouritesImagesList.add(i, goodsInfo[1]);
                    favouritesNamesList.add(i, goodsInfo[2]+"\n"+goodsInfo[3]);
                    favouritesPricesList.add(i, infoLoaded.get(i));
                    if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                        if (Integer.parseInt(goodsInfo[4].replace(" ", ""). replace("грн", "")) != 0){
                            favouritesPricesList.add(i,  "АКЦИЯ\nЗачёркнутая цена:\n" + goodsInfo[4] + "\nСейчас цена:\n" +  goodsInfo[5]);
                        } else{
                            favouritesPricesList.add(i,  goodsInfo[5]);
                        }
                    } else {
                            favouritesPricesList.add(i, "Ціна формується");
                        }

                }
                adapter1.notifyDataSetChanged();



            }else{
                Toast.makeText(ChosenGoodsActivity.this, "Загрузились не все товары, подождите", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<String> rUrl = new ArrayList<>();

        String str1 = "";
        String str2 = "";

                MyAdapter(Context c, List<String> title, List<String> description, List<String> imgs, List<String> urls) {
            super(c, R.layout.goods_view, R.id.goodsTitleMain, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rImgs = imgs;
            this.rUrl = urls;

        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View goodsView = layoutInflater.inflate(R.layout.goods_view, parent, false);
            ImageView images = goodsView.findViewById(R.id.goodsImageMain);
            ImageView imageDelete = goodsView.findViewById(R.id.goodsFavourite);
            TextView myTitle = goodsView.findViewById(R.id.goodsTitleMain);
            TextView myDescription = goodsView.findViewById(R.id.goodsPriceMain);

            // now set our resources on views


            Glide.with(context).load(rImgs.get(position)).into(images);
            myTitle.setText(rTitle.get(position));
            try {
                myDescription.setText(rDescription.get(position));
            }  catch (Exception e) {
                e.printStackTrace();
            }

            imageDelete.setImageResource(R.drawable.ic_baseline_delete_24);

            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    //Open link from favourites

                    //------------------------------------------------------------------------------------------

                    if (isLoadedRozetka){
                        mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();

                                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                                    Map<String,Object> taskMap = new HashMap<String,Object>();
                                    objRef.updateChildren(taskMap);




                                    String str = favouritesNamesList.get(position).replace("\nCITRUS", "").replace("\nALLO", "").replace("\nROZETKA", "");

                                    Log.d("TEST", position +", "+str+", "+childDataSnapshot.getValue());
                                    if (String.valueOf(childDataSnapshot.getValue()).contains(str)){


                                        for (int i = 0; i < infoLoaded.size(); i++) {
                                            str1 = infoLoaded.get(i).split("SPLIT", 6)[2].replace("CITRUS", "").replace("ALLO", "").replace("ROZETKA", "");
                                            str2 = favouritesNamesList.get(position).replace("CITRUS", "").replace("ALLO", "").replace("ROZETKA", "");

                                            Log.d("TEST1", str1);

                                            if (str2.contains(str1)){

                                                Log.d("TEST2", str1);

                                                infoLoaded.remove(i);

                                                break;
                                            }
                                        }


                                        infoLoadedUp.clear();
                                        for (int i = 0; i < infoLoaded.size(); i++) {
                                            infoLoadedUp.add(i, infoLoaded.get(i));
                                        }

                                        favouritesUrlList.remove(favouritesList.size()-1-position);
                                        favouritesImagesList.remove(position);
                                        favouritesNamesList.remove(position);
                                        favouritesPricesList.remove(position);
                                        //favouritesList.remove(favouritesList.size()-1-position);

                                        adapter1.notifyDataSetChanged();

                                        mDatabase.child("Users").child(user.getUid()).child(childDataSnapshot.getKey()).setValue(null);
                                        mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                        break;
                                    }
                                    mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else{
                        Toast.makeText(ChosenGoodsActivity.this, "Загрузились не все товары, подождите", Toast.LENGTH_SHORT).show();
                    }



                    //------------------------------------------------------------------------------------------
                }
            });

            return goodsView;
        }
    }



}
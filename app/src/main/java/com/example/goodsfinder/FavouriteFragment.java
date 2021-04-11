package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouriteFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
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

    boolean isNotEmpty = false;

    private List<String> urlsRozetka = new ArrayList<String>();
    private List<String> positionsRozetka = new ArrayList<String>();

    private List<String> infoLoaded = new ArrayList<String>();
    private List<String> infoLoadedUp = new ArrayList<String>();

    TextView errorTextChoosen;
    ProgressBar progressBar;
    TextView emptyTextChoosen;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    WebView browserRozetka;

    ArrayList<String> favouritesImagesList = new ArrayList<>();
    ArrayList<String> favouritesStoreImagesList = new ArrayList<>();
    ArrayList<String> favouritesOldPricesList = new ArrayList<>();
    ArrayList<String> favouritesPricesList = new ArrayList<>();
    ArrayList<String> favouritesNamesList = new ArrayList<>();
    ArrayList<String> favouritesUrlList = new ArrayList<>();
    ArrayList<Integer> favouritesColourList = new ArrayList<>();



    ArrayList<String> favouritesList = new ArrayList<>();

    ListView goodsListView;
    MyAdapter adapter1;


    FirebaseListAdapter mAdapter;

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


    private void loadChoosen(){

        mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, Object> td = (HashMap<String, Object>) snapshot.getValue();
                favouritesUrlList.clear();
                favouritesColourList.clear();
                favouritesImagesList.clear();
                favouritesStoreImagesList.clear();
                favouritesNamesList.clear();
                favouritesPricesList.clear();
                favouritesOldPricesList.clear();
                infoLoaded.clear();
                infoLoadedUp.clear();
                favouritesList.clear();

                boolean isNotEmpty = false;

                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    isNotEmpty = true;

                    int counter = 0;
                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child(childDataSnapshot.getKey());
                    Map<String, Object> taskMap = new HashMap<String, Object>();
                    objRef.updateChildren(taskMap);


                    favouritesList.add(String.valueOf(childDataSnapshot.getValue()));
                    String[] goodsInfo = String.valueOf(childDataSnapshot.getValue()).split("SPLITFORBUY", 4);


                    favouritesUrlList.add(counter, goodsInfo[0]);
                    favouritesImagesList.add(counter, goodsInfo[1]);
                    favouritesNamesList.add(counter, goodsInfo[2]);


                    if(goodsInfo[3].equals("ROZETKA")){
                        favouritesStoreImagesList.add(counter,"https://mmr.ua/uploaded/materials/a2a89af751.png");
                        favouritesColourList.add(counter,ContextCompat.getColor(context,R.color.rozetka_colour));
                    }else if(goodsInfo[3].equals("ALLO")){
                        favouritesStoreImagesList.add(counter,"data:image/png;base64,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");
                        favouritesColourList.add(counter, ContextCompat.getColor(context,R.color.allo_colour));
                    }if(goodsInfo[3].equals("CITRUS")){
                        favouritesStoreImagesList.add(counter,"https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");
                        favouritesColourList.add(counter,ContextCompat.getColor(context,R.color.citrus_colour));
                    }

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
                                    favouritesPricesList.add(counter, price1);
                                    favouritesOldPricesList.add(counter, price2);

                                    infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+price2+"SPLITFORBUY"+price1);
                                } else {
                                    favouritesPricesList.add(counter,  price1);
                                    favouritesOldPricesList.add(counter, " ");
                                    infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+"0"+"SPLITFORBUY"+price1);
                                }



                            } else {
                                favouritesPricesList.add(counter, getString(R.string.form_price));
                                favouritesOldPricesList.add(counter, " ");
                            }

                        } catch (IOException e) {
                            mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                            e.printStackTrace();
                        }
                    } else if (goodsInfo[0].contains("rozetka.com.ua")) {
                        mDatabase.child("Users").child(user.getUid()).removeEventListener(this);


                        for (int i = 0; i < positionsRozetka.size(); i++) {
                            positionsRozetka.set(i, String.valueOf(Integer.parseInt(positionsRozetka.get(i)) + 1));
                        }
                        urlsRozetka.add(goodsInfo[0]);
                        positionsRozetka.add(String.valueOf(counter));

                        favouritesPricesList.add(counter,  getString(R.string.loading));
                        favouritesOldPricesList.add(counter,  getString(R.string.loading));
                        infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY");

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
                                    favouritesPricesList.add(counter, price1);
                                    favouritesOldPricesList.add(counter, price2);
                                    infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+price2+"SPLITFORBUY"+price1);
                                } else {
                                    favouritesPricesList.add(counter,  price1);
                                    favouritesOldPricesList.add(counter, " ");
                                    infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+"0"+"SPLITFORBUY"+price1);
                                }

                            } else {
                                favouritesPricesList.add(counter, getString(R.string.form_price));
                                favouritesOldPricesList.add(counter, " ");
                                infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+"0"+"SPLITFORBUY"+"0");
                            }



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    adapter1.notifyDataSetChanged();

                    counter++;

                }

                if (!isNotEmpty){
                    emptyTextChoosen.setVisibility(View.VISIBLE);
                }

                progressBar.setVisibility(View.GONE);

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
                mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite2,container,false);

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.chosen_goods_activity);

        String[] filters = { getString(R.string.def),  getString(R.string.up), getString(R.string.down)};

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, filters);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = (String)parent.getItemAtPosition(position);

                if (item.equals(getString(R.string.def)) && isLoadedRozetka){
                    sortDefault();
                    Log.d("TETS", item);
                } else if (item.equals(getString(R.string.up))){
                    sortUp();
                    Log.d("TETS", item);
                } else if (item.equals(getString(R.string.down))){
                    sortDown();
                    Log.d("TETS", item);
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);

        errorTextChoosen = view.findViewById(R.id.errorTextChoosen);
        emptyTextChoosen = view.findViewById(R.id.emptyTextChoosen);
        errorTextChoosen.setVisibility(View.INVISIBLE);
        emptyTextChoosen.setVisibility(View.INVISIBLE);

        progressBar = view.findViewById(R.id.progressBarChoosen);


        if (!isConnected()){
            progressBar.setVisibility(View.GONE);
            errorTextChoosen.setVisibility(View.VISIBLE);
        } else {
            loadChoosen();
        }

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        browserRozetka = new WebView(getActivity());
        getMoyo();
        view.findViewById(R.id.btn_reload).setOnClickListener(this::onClick);

        goodsListView = (ListView) view.findViewById(R.id.favouritesView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        adapter1 = new MyAdapter(getActivity(),favouritesNamesList,favouritesPricesList,favouritesOldPricesList,favouritesImagesList,favouritesStoreImagesList ,favouritesUrlList,favouritesColourList);
        goodsListView.setAdapter(adapter1);

        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Uri address = Uri.parse(favouritesUrlList.get(i));
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);
            }

        });

        return view;

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
                                        favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), price1);
                                        favouritesOldPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  price2);
                                        infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+price2+"SPLITFORBUY"+price1);
                                    } else {
                                        favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  price1);
                                        favouritesOldPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  " ");
                                        infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+"0"+"SPLITFORBUY"+price1);
                                    }




                                } else {
                                    favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), getString(R.string.form_price));
                                    favouritesOldPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  " ");
                                }

                                //favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), priceRozetka.replace("₴", "").replace("грн", "").replace(" грн", "") + " грн");
                                adapter1.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);


                                getActivity().runOnUiThread(new Runnable() {
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

        if(view.getId() == R.id.btn_reload) {
            if (isConnected()){
                errorTextChoosen.setVisibility(View.INVISIBLE);
                emptyTextChoosen.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                favouritesUrlList.clear();
                favouritesImagesList.clear();
                favouritesStoreImagesList.clear();
                favouritesNamesList.clear();
                favouritesPricesList.clear();
                favouritesOldPricesList.clear();
                favouritesColourList.clear();

                adapter1.notifyDataSetChanged();

                loadChoosen();
            } else {
                Toast.makeText(getActivity(),getString(R.string.internet_error),Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void sortDefault(){
        if (isLoadedRozetka){

            Log.d("COMMON", String.valueOf(infoLoaded));

            favouritesUrlList.clear();
            favouritesImagesList.clear();
            favouritesStoreImagesList.clear();
            favouritesNamesList.clear();
            favouritesPricesList.clear();
            favouritesOldPricesList.clear();
            favouritesColourList.clear();

            for (int i = 0; i < infoLoaded.size(); i++) {
                String[] goodsInfo = String.valueOf(infoLoaded.get(infoLoaded.size()-1-i)).split("SPLITFORBUY", 6);
                favouritesUrlList.add(i, goodsInfo[0]);
                favouritesImagesList.add(i, goodsInfo[1]);
                favouritesNamesList.add(i, goodsInfo[2]);
                if(goodsInfo[3].equals("ROZETKA")){
                    favouritesStoreImagesList.add(i,"https://mmr.ua/uploaded/materials/a2a89af751.png");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,"data:image/png;base64,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");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,"https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.citrus_colour));
                }
                favouritesPricesList.add(i, infoLoaded.get(i));
                favouritesOldPricesList.add(i, infoLoadedUp.get(i));

                if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                    if (Integer.parseInt(goodsInfo[4].replace(" ", ""). replace("грн", "")) != 0){
                        favouritesPricesList.add(i,  goodsInfo[5]);
                        favouritesOldPricesList.add(i,  goodsInfo[4]);
                    } else{
                        favouritesPricesList.add(i,  goodsInfo[5]);
                        favouritesOldPricesList.add(i, " ");
                    }
                } else {
                    favouritesPricesList.add(i, getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }

            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
        }
    }
    public void sortDown(){
        if (isLoadedRozetka){
            Log.d("TEST", String.valueOf(infoLoaded));
            boolean sorted = false;
            String temp;
            while(!sorted) {
                sorted = true;
                for (int i = 0; i < infoLoadedUp.size() - 1; i++) {

                    String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLITFORBUY", 6);
                    String[] goodsInfoNext = String.valueOf(infoLoadedUp.get(i+1)).split("SPLITFORBUY", 6);

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
            favouritesStoreImagesList.clear();
            favouritesNamesList.clear();
            favouritesPricesList.clear();
            favouritesOldPricesList.clear();
            favouritesColourList.clear();

            for (int i = 0; i < infoLoadedUp.size(); i++) {
                String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLITFORBUY", 6);
                favouritesUrlList.add(i, goodsInfo[0]);
                favouritesImagesList.add(i, goodsInfo[1]);
                favouritesNamesList.add(i, goodsInfo[2]);
                if(goodsInfo[3].equals("ROZETKA")){
                    favouritesStoreImagesList.add(i,"https://mmr.ua/uploaded/materials/a2a89af751.png");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,"data:image/png;base64,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");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,"https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.citrus_colour));
                }
                favouritesPricesList.add(i, infoLoadedUp.get(i));
                favouritesOldPricesList.add(i, infoLoadedUp.get(i));

                if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                    if (Integer.parseInt(goodsInfo[4].replace(" ", ""). replace("грн", "")) != 0){
                        favouritesPricesList.add(i, goodsInfo[5]);
                        favouritesOldPricesList.add(i,goodsInfo[4]);
                    } else{
                        favouritesPricesList.add(i,  goodsInfo[5]);
                        favouritesOldPricesList.add(i, " ");
                    }
                }else {
                    favouritesPricesList.add(i, getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }

            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
        }
    }

    public void sortUp(){
        if (isLoadedRozetka){
            Log.d("BEFORE", String.valueOf(infoLoaded));
            boolean sorted = false;
            String temp;
            while(!sorted) {
                sorted = true;
                for (int i = 0; i < infoLoadedUp.size() - 1; i++) {

                    String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLITFORBUY", 6);
                    String[] goodsInfoNext = String.valueOf(infoLoadedUp.get(i+1)).split("SPLITFORBUY", 6);

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
            favouritesStoreImagesList.clear();
            favouritesNamesList.clear();
            favouritesPricesList.clear();
            favouritesOldPricesList.clear();
            favouritesColourList.clear();

            for (int i = 0; i < infoLoadedUp.size(); i++) {
                Log.d("TETS", String.valueOf(infoLoadedUp.size() + " " + infoLoaded.size()));
                String[] goodsInfo = String.valueOf(infoLoadedUp.get(i)).split("SPLITFORBUY", 6);
                favouritesUrlList.add(i, goodsInfo[0]);
                favouritesImagesList.add(i, goodsInfo[1]);
                favouritesNamesList.add(i, goodsInfo[2]);
                if(goodsInfo[3].equals("ROZETKA")){
                    favouritesStoreImagesList.add(i,"https://mmr.ua/uploaded/materials/a2a89af751.png");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,"data:image/png;base64,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");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,"https://yt3.ggpht.com/ytc/AAUvwnjVEgy0xS7qiFpem68qOwYiIBi4Fls8dZYw9EFm1A=s900-c-k-c0x00ffffff-no-rj");
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.citrus_colour));
                }
                favouritesPricesList.add(i, infoLoadedUp.get(i));
                favouritesOldPricesList.add(i, infoLoadedUp.get(i));

                if (Integer.parseInt(goodsInfo[5].replace(" ", ""). replace("грн", "")) != 0) {
                    if (Integer.parseInt(goodsInfo[4].replace(" ", "").replace("грн", "")) != 0) {
                        favouritesPricesList.add(i, goodsInfo[5]);
                        favouritesOldPricesList.add(i, goodsInfo[4]);
                    } else {
                        favouritesPricesList.add(i, goodsInfo[5]);
                        favouritesOldPricesList.add(i, " ");
                    }
                } else {
                    favouritesPricesList.add(i, getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }


            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
        }
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rOldDescription = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<String> rStoreImgs = new ArrayList();
        List<String> rUrl = new ArrayList<>();
        List<Integer> backgroundColour = new ArrayList<>();

        String str1 = "";
        String str2 = "";

        MyAdapter(Context c, List<String> title, List<String> description , List<String> oldDescription, List<String> imgs, List<String> storeImgs, List<String> urls, List<Integer> backgroundColor) {
            super(c, R.layout.goods_view, R.id.goodsTitleMain, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rOldDescription = oldDescription;
            this.rImgs = imgs;
            this.rStoreImgs = storeImgs;
            this.rUrl = urls;
            this.backgroundColour = backgroundColor;

        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View goodsView = layoutInflater.inflate(R.layout.goods_view, parent, false);
            ImageView images = goodsView.findViewById(R.id.goodsImageMain);
            ImageView storeImages = goodsView.findViewById(R.id.storeImageMain);
            ImageView imageDelete = goodsView.findViewById(R.id.goodsFavourite);
            // ImageView imageBackground = goodsView.findViewById(R.id.goodsBackground);
            CardView backgroundCard = goodsView.findViewById(R.id.backgroungCard);
            TextView myTitle = goodsView.findViewById(R.id.goodsTitleMain);
            TextView myDescription = goodsView.findViewById(R.id.goodsPriceMain);
            TextView myOldDescription = goodsView.findViewById(R.id.oldGoodsPriceMain);

            // now set our resources on views


            Glide.with(context).load(rImgs.get(position)).into(images);
            Glide.with(context).load(rStoreImgs.get(position)).into(storeImages);
            backgroundCard.setCardBackgroundColor(backgroundColour.get(position));


            myTitle.setText(rTitle.get(position));
            try {
                myDescription.setText(rDescription.get(position));
                myOldDescription.setText(rOldDescription.get(position));
            }  catch (Exception e) {
                e.printStackTrace();
            }

            imageDelete.setImageResource(R.drawable.ic_baseline_delete_24);


            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //imageDelete.setClickable(false);


                    //------------------------------------------------------------------------------------------

                    if (isLoadedRozetka){
                        if (isConnected()){
                            mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();

                                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                        DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                                        Map<String,Object> taskMap = new HashMap<String,Object>();
                                        objRef.updateChildren(taskMap);



                                        try {
                                            String str = favouritesNamesList.get(position).replace("\nCITRUS", "").replace("\nALLO", "").replace("\nROZETKA", "");

                                            if (String.valueOf(childDataSnapshot.getValue()).contains(str)){


                                                for (int i = 0; i < infoLoaded.size(); i++) {
                                                    str1 = infoLoaded.get(i).split("SPLITFORBUY", 6)[2].replace("CITRUS", "").replace("ALLO", "").replace("ROZETKA", "");
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

                                                try {
                                                    favouritesUrlList.remove(favouritesUrlList.size()-1-position);
                                                    favouritesImagesList.remove(position);
                                                    favouritesNamesList.remove(position);
                                                    favouritesPricesList.remove(position);
                                                    //favouritesList.remove(favouritesList.size()-1-position);

                                                    adapter1.notifyDataSetChanged();
                                                    progressBar.setVisibility(View.GONE);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                adapter1.notifyDataSetChanged();
                                                progressBar.setVisibility(View.GONE);

                                                mDatabase.child("Users").child(user.getUid()).child(childDataSnapshot.getKey()).setValue(null);
                                                mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                                errorTextChoosen.setVisibility(View.INVISIBLE);
                                                emptyTextChoosen.setVisibility(View.INVISIBLE);
                                                progressBar.setVisibility(View.VISIBLE);

                                                favouritesUrlList.clear();
                                                favouritesImagesList.clear();
                                                favouritesStoreImagesList.clear();
                                                favouritesNamesList.clear();
                                                favouritesPricesList.clear();
                                                favouritesOldPricesList.clear();
                                                favouritesColourList.clear();

                                                adapter1.notifyDataSetChanged();

                                                loadChoosen();

                                                break;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            //Toast.makeText(ChosenGoodsActivity.this, "Загрузились не все товары, подождите", Toast.LENGTH_SHORT).show();
                                        }



                                        mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else{
                            Toast.makeText(getActivity(),getString(R.string.internet_error),Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(getActivity(),getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
                    }



                    //------------------------------------------------------------------------------------------
                }
            });

            return goodsView;
        }
    }



}
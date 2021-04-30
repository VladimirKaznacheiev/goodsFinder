package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
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
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.OnBalloonDismissListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouriteFragment extends Fragment {
    Balloon balloon1;
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
    ArrayList<Integer> favouritesStoreImagesList = new ArrayList<>();
    ArrayList<String> favouritesOldPricesList = new ArrayList<>();
    ArrayList<String> favouritesPricesList = new ArrayList<>();
    ArrayList<String> favouritesNamesList = new ArrayList<>();
    ArrayList<String> favouritesUrlList = new ArrayList<>();
    ArrayList<Integer> favouritesColourList = new ArrayList<>();

    Balloon balloon5;
    Balloon balloon6;
    Balloon balloon7;



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


    public void loadChoosen(){

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
                urlsRozetka.clear();

                boolean isNotEmpty = false;
                Log.d("RELOADING", "reloading");
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


                    if(goodsInfo[3].equals("ELDORADO")){
                        favouritesStoreImagesList.add(counter,R.drawable.eldoradologo);
                        favouritesColourList.add(counter,ContextCompat.getColor(context,R.color.rozetka_colour));
                    }else if(goodsInfo[3].equals("ALLO")){
                        favouritesStoreImagesList.add(counter,R.drawable.allologo);
                        favouritesColourList.add(counter, ContextCompat.getColor(context,R.color.allo_colour));
                    }if(goodsInfo[3].equals("CITRUS")){
                        favouritesStoreImagesList.add(counter,R.drawable.citruslogo);
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
                                favouritesPricesList.add(counter, context.getString(R.string.form_price));
                                favouritesOldPricesList.add(counter, " ");
                                infoLoaded.add(goodsInfo[0]+"SPLITFORBUY"+goodsInfo[1]+"SPLITFORBUY"+goodsInfo[2]+"SPLITFORBUY"+goodsInfo[3]+"SPLITFORBUY"+"0"+"SPLITFORBUY"+"0");
                            }

                        } catch (IOException e) {
                            mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                            e.printStackTrace();
                        }
                    } else if (goodsInfo[0].contains("https://eldorado.ua")) {
                        mDatabase.child("Users").child(user.getUid()).removeEventListener(this);



                        for (int i = 0; i < positionsRozetka.size(); i++) {
                            positionsRozetka.set(i, String.valueOf(Integer.parseInt(positionsRozetka.get(i)) + 1));
                        }
                        urlsRozetka.add(goodsInfo[0]);
                        positionsRozetka.add(String.valueOf(counter));

                        favouritesPricesList.add(counter,  context.getString(R.string.loading));
                        favouritesOldPricesList.add(counter,  context.getString(R.string.loading));
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
                                favouritesPricesList.add(counter, context.getString(R.string.form_price));
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
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite2,container,false);

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.chosen_goods_activity);

        String[] filters = { context.getString(R.string.def),  context.getString(R.string.up), context.getString(R.string.down)};

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, filters);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        //BALOONS


        balloon5 = new Balloon.Builder(context)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(context.getResources().getString((R.string.balloon5)))
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {
                        balloon6.showAlignBottom(spinner);
                    }
                })
                .build();

        balloon6 = new Balloon.Builder(context)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(context.getResources().getString((R.string.balloon6)))
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {
                        balloon7.showAlignBottom(view.findViewById(R.id.btn_reload));
                    }
                })
                .build();

        balloon7 = new Balloon.Builder(context)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.13f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(context.getResources().getString((R.string.balloon7)))
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {
                        Toolbar tb = ((BottomNavigationActivity)getContext()).toolbar;

                        ((BottomNavigationActivity)getContext()).balloon8.showAlignRight(((BottomNavigationActivity) getContext()).toolbar.findViewById(R.id.ghostTxt));
                    }
                })
                .build();

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = (String)parent.getItemAtPosition(position);

                if (item.equals(context.getString(R.string.def)) && isLoadedRozetka){
                    sortDefault();
                    Log.d("TETS", item);
                } else if (item.equals(context.getString(R.string.up))){
                    Log.d("ELDORADO", String.valueOf(isLoadedRozetka));
                    sortUp();
                    Log.d("TETS", item);
                } else if (item.equals(context.getString(R.string.down))){
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
                                Log.d("ELDORADO", doc1.getElementsByAttributeValue("class", "price-value").last().text());
                                //Log.d("ELDORADO", doc1.getElementsByAttributeValue("class", "base-slick slick-slide-item").last().html());
                                priceRozetka = doc1.getElementsByAttributeValue("class", "price-value ").last().text();
                                Element oldPriceRozetka = doc1.getElementsByAttributeValue("class", "old-price-value").last();




                                if (priceRozetka!=null){
                                    String price1;
                                    if (!priceRozetka.contains(context.getString(R.string.form_price))){
                                        price1 = priceRozetka.replace("₴", "").replace("грн", "").replace(" грн", "").replace(".", "") + " грн";
                                    } else {
                                        price1 = priceRozetka;
                                    }


                                    if (oldPriceRozetka!=null){
                                        String price2 = oldPriceRozetka.text().replace("₴", "").replace("грн", "").replace(" грн", "").replace(".", "") + " грн";
                                        Log.d("ELDORADOLOG", String.valueOf(positionsRozetka+", "+iRozetka+", "+favouritesPricesList.size()));
                                        favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), price1);
                                        favouritesOldPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  price2);
                                        infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+price2+"SPLITFORBUY"+price1);
                                    } else {
                                        favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  price1);
                                        favouritesOldPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  " ");
                                        infoLoaded.set(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)),  infoLoaded.get(infoLoaded.size()-1-Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)))+"0"+"SPLITFORBUY"+price1);
                                    }




                                } else {
                                    favouritesPricesList.set(Integer.parseInt(positionsRozetka.get(positionsRozetka.size()-1-iRozetka)), context.getString(R.string.form_price));
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

                                            Log.d("ELDORADO", "TRUE");
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

            reload();

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
                if(goodsInfo[3].equals("ELDORADO")){
                    favouritesStoreImagesList.add(i,R.drawable.eldoradologo);                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,R.drawable.allologo);
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,R.drawable.citruslogo);
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
                    favouritesPricesList.add(i, context.getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }

            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),context.getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
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
                if(goodsInfo[3].equals("ELDORADO")){
                    favouritesStoreImagesList.add(i,R.drawable.eldoradologo);
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,R.drawable.allologo);
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,R.drawable.citruslogo);
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
                    favouritesPricesList.add(i, context.getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }

            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),context.getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
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
                if(goodsInfo[3].equals("ELDORADO")){
                    favouritesStoreImagesList.add(i,R.drawable.eldoradologo);
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.rozetka_colour));
                }else if(goodsInfo[3].equals("ALLO")){
                    favouritesStoreImagesList.add(i,R.drawable.allologo);
                    favouritesColourList.add(i,ContextCompat.getColor(context,R.color.allo_colour));
                }if(goodsInfo[3].equals("CITRUS")){
                    favouritesStoreImagesList.add(i,R.drawable.citruslogo);
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
                    favouritesPricesList.add(i, context.getString(R.string.form_price));
                    favouritesOldPricesList.add(i, " ");
                }


            }
            adapter1.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);



        }else{
            Toast.makeText(getActivity(),context.getString(R.string.goods_no_loaded),Toast.LENGTH_SHORT).show();
        }
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rOldDescription = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<Integer> rStoreImgs = new ArrayList();
        List<String> rUrl = new ArrayList<>();
        List<Integer> backgroundColour = new ArrayList<>();

        String str1 = "";
        String str2 = "";

        MyAdapter(Context c, List<String> title, List<String> description , List<String> oldDescription, List<String> imgs, List<Integer> storeImgs, List<String> urls, List<Integer> backgroundColor) {
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


            myDescription.setTextColor(ContextCompat.getColor(context,R.color.price_color));
            myOldDescription.setTextColor(ContextCompat.getColor(context,R.color.oldprice_colour));
            Glide.with(context).load(rImgs.get(position)).into(images);
            storeImages.setImageResource(rStoreImgs.get(position));
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
                                            String str = favouritesNamesList.get(position).replace("\nCITRUS", "").replace("\nALLO", "").replace("\nELDORADO", "");

                                            if (String.valueOf(childDataSnapshot.getValue()).contains(str)){


                                                for (int i = 0; i < infoLoaded.size(); i++) {
                                                    str1 = infoLoaded.get(i).split("SPLITFORBUY", 6)[2].replace("CITRUS", "").replace("ALLO", "").replace("ELDORADO", "");
                                                    str2 = favouritesNamesList.get(position).replace("CITRUS", "").replace("ALLO", "").replace("ELDORADO", "");

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
                            Toast.makeText(getActivity(),context.getString(R.string.internet_error),Toast.LENGTH_SHORT).show();
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

    public void learning1(){
        balloon5.showAlignBottom(errorTextChoosen);
    }

    public void reload() {


        if (isConnected()) {

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
            positionsRozetka.clear();
            isLoadedRozetka = false;
            iRozetka = 0;

            infoLoaded.clear();
            infoLoadedUp.clear();
            favouritesList.clear();
            urlsRozetka.clear();
            adapter1.notifyDataSetChanged();


            loadChoosen();
        } else {
            Toast.makeText(getActivity(), context.getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
        }
    }

}
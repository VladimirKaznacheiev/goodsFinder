package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChosenGoodsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private List<String> DiscrTasks;

    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


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

        findViewById(R.id.btn_to_main).setOnClickListener(this::onClick);

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
                Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();
                favouritesUrlList.clear();
                favouritesImagesList.clear();
                favouritesNamesList.clear();
                favouritesPricesList.clear();
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    int counter = 0;
                    DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                    Map<String,Object> taskMap = new HashMap<String,Object>();
                    objRef.updateChildren(taskMap);
                    //Log.d("Testing",""+ childDataSnapshot.getValue());


                    favouritesList.add(String.valueOf(childDataSnapshot.getValue()));
                    String[] goodsInfo = String.valueOf(childDataSnapshot.getValue()).split("split", 4);

                        //Log.d("Testing",String.valueOf(childDataSnapshot.getValue()).split("split", 4)[3]);



                        favouritesUrlList.add(counter, goodsInfo[0]);
                        favouritesImagesList.add(counter, goodsInfo[1]);
                        favouritesNamesList.add(counter, goodsInfo[2]);
                        favouritesPricesList.add(counter, goodsInfo[3].replaceAll(" грн","")+" грн");

                        adapter1.notifyDataSetChanged();
                        counter++;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void onClick(View view) {
        if(view.getId() == R.id.btn_to_main) {
            Intent intent = new Intent(ChosenGoodsActivity.this, MainActivity.class);
            startActivity(intent);

        }
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> rTitle = new ArrayList();
        List<String> rDescription = new ArrayList();
        List<String> rImgs = new ArrayList();
        List<String> rUrl = new ArrayList<>();


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
            myDescription.setText(rDescription.get(position));
            imageDelete.setImageResource(R.drawable.ic_baseline_delete_24);

            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                //Open link from favourites

                    //------------------------------------------------------------------------------------------

                    mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Object> td = (HashMap<String,Object>) snapshot.getValue();

                            for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                                DatabaseReference objRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child( childDataSnapshot.getKey());
                                Map<String,Object> taskMap = new HashMap<String,Object>();
                                objRef.updateChildren(taskMap);


                                String str = favouritesList.get(favouritesList.size()-position-1);

                                if (childDataSnapshot.getValue().equals(str)){

                                    favouritesList.clear();
                                    adapter1.notifyDataSetChanged();

                                    //Log.d("Testing1",""+ favouritesList.size());
                                    //Log.d("Testing2",""+  childDataSnapshot.getKey());
                                    mDatabase.child("Users").child(user.getUid()).child(childDataSnapshot.getKey()).setValue(null);
                                    mDatabase.child("Users").child(user.getUid()).removeEventListener(this);

                                    if (position == 0){
                                        favouritesList.clear();
                                        favouritesUrlList.clear();
                                        favouritesImagesList.clear();
                                        favouritesNamesList.clear();
                                        favouritesPricesList.clear();
                                        adapter1.notifyDataSetChanged();
                                    }

                                    break;
                                }
                                mDatabase.child("Users").child(user.getUid()).removeEventListener(this);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //------------------------------------------------------------------------------------------
                }
            });

            return goodsView;
        }
    }



}



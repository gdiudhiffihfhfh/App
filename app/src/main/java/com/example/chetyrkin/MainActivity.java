package com.example.chetyrkin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String KEY ="f455e007fa5d929a874e";



    Spinner spinner;
    Spinner spinner2;

    EditText ed1;
    EditText ed2;
    boolean who;//who change editText last
    Double coefficient =1.0; //  currensy1:currensy2(devide)
    int flagForEdit=0; //flag for editText against loops
    boolean flagForInternet;
    static final String KEYFORK="K1";
    static final String KEYFORK1="K";
    static final String KEYFORK2="O";
    static final String KEYFOR1="1";
    static final String KEYFOR2="2";

    String[] currency ={"EUR","USD","GBP","RUB","ALL","XCD","BBD","BTN","BND","XAF","CUP"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        ed1=findViewById(R.id.editText);
        ed2=findViewById(R.id.editText2);



        ed1.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                flagForEdit=0;
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                if(flagForEdit!=2){//editText2 cant change editText1
                    who=false;
                    flagForEdit=1;
                    if(ed1.getText().toString().equals("")){
                        ed2.setText("");
                    }
                    else{
                        ed2.setText(Double.parseDouble(ed1.getText().toString())*coefficient+"");
                    }
                }


            }
        });
        ed2.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                flagForEdit=0;


            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                if(flagForEdit!=1){
                    who=true;
                    flagForEdit=2;
                    if(ed2.getText().toString().equals("")){
                        ed1.setText("");
                    }
                    else{
                        ed1.setText(Double.parseDouble(ed2.getText().toString())/coefficient+"");
                    }
                }

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, currency);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);


        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,final View view, int position, long id) {
                String currensy1=spinner.getSelectedItem().toString();
                String currensy2=spinner2.getSelectedItem().toString();
                if(currensy1.equals(currency[0]) && currensy2.equals(currency[0])){ //write for method query wasn't called at the beginning
                    coefficient=1.0;
                    return;
                }
                if(!isOnline()){//check internet
                    ed1.setEnabled(false);
                    ed2.setEnabled(false);
                    flagForInternet=true;
                    spinner.setSelection(0);
                    spinner2.setSelection(0);
                    ed1.setText("");
                    ed2.setText("");
                    Toast toast =
                            Toast.makeText(getApplicationContext(),"Отсутсвует интернет", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;

                }
                flagForInternet=false;
                ed1.setEnabled(true);
                ed2.setEnabled(true);
                query(currensy1,currensy2)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String st) {

                                if(st.equals("-1")){
                                    Toast toast =
                                            Toast.makeText(getApplicationContext(),"Ошибка сервера", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                                else{
                                    coefficient=Double.parseDouble(st);
                                    if(!(ed1.getText().toString().equals("")|| ed2.getText().equals(""))){ // it is imposible if one has value and another has not
                                        if(who){
                                            ed1.setText(Double.parseDouble(ed2.getText().toString())/coefficient+"");
                                            who=true;

                                        }
                                        else{
                                            ed2.setText(Double.parseDouble(ed1.getText().toString())*coefficient+"");
                                            who=false;

                                        }

                                    }

                                }






                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
        spinner2.setOnItemSelectedListener(itemSelectedListener);


    }
    Observable<String> query(final String str1, final String str2){
        return Observable.create(new ObservableOnSubscribe<String >() {
            @Override
            public void subscribe(final ObservableEmitter<String > emitter) {

                    ApiService api = RetroClient.getApiService();
                    Map<String,String>hm = new HashMap<>();

                    hm.put("q",str1+"_"+str2);
                    hm.put("compact","ultra");
                    hm.put("apiKey", KEY);

                    Call<ResponseBody> call = api.getMyJSON(hm);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            try {
                                String answer = response.body().string();
                                String koef=answer.substring(answer.indexOf(':')+1,answer.length()-1);
                                emitter.onNext(koef);
                            } catch (Exception e) {
                                emitter.onNext("-1");
                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            emitter.onNext("-1");
                        }
                    });


            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEYFOR1,spinner.getSelectedItemPosition());
        outState.putInt(KEYFOR2,spinner2.getSelectedItemPosition());
        outState.putString(KEYFORK1,ed1.getText().toString());
        outState.putString(KEYFORK2, ed2.getText().toString());

        outState.putDouble(KEYFORK,coefficient);

        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {


        spinner.setSelection(savedInstanceState.getInt(KEYFOR1));
        spinner2.setSelection(savedInstanceState.getInt(KEYFOR2));
        ed1.setText(savedInstanceState.getString(KEYFORK1));
        ed2.setText(savedInstanceState.getString(KEYFORK2));
        coefficient=savedInstanceState.getDouble(KEYFORK);
        super.onRestoreInstanceState(savedInstanceState);


    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }




}

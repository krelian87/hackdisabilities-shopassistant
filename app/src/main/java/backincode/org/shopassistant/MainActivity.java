package backincode.org.shopassistant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements BeaconConsumer{

    protected static final String TAG = "BeaconListActivity";
    private BeaconManager beaconManager;
    private Map<String, String> beaconToSector = new HashMap<String, String>();
    private TextView area;
    private Button areaButton;
    private Button helpButton;
    private Button cameraButton;


    private String areaName="";
    private TextToSpeech tts;
    private boolean speakOK = false;
    private Handler mHandler =new Handler(){

        public void handleMessage(Message input){
            refreshBeacons((ArrayList<Beacon>) input.obj);
        }

    };

    TextView test;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.beaconToSector.put("13", "Salumeria");
        this.beaconToSector.put("12", "Ortofrutta");


        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setForegroundScanPeriod(TimeUnit.SECONDS.toMillis(1));
        beaconManager.setBackgroundBetweenScanPeriod(TimeUnit.SECONDS.toMillis(0));
        beaconManager.setForegroundBetweenScanPeriod(TimeUnit.SECONDS.toMillis(0));
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1));

        beaconManager.bind(this);
        mBeaconLayout = (LinearLayout)findViewById(R.id.BeaconListLayout);
        //test=new TextView(getApplicationContext());
        //test.setText("base " + Math.random());
        //mBeaconLayout.addView(test);
        //this.area = (TextView)findViewById(R.id.area);

        mBeacons=new HashMap<>();
        addListenerOnButtonArea();
        addListenerOnButtonHelp();
        //addListenerOnButtonCamera();

        this.tts = new TextToSpeech(this.getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.ITALY);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                        speakOK = false;
                    }
                    else{
                        speakOK = true;

                        //                        String id = et.getText().toString();
                        //String speak = "Prova testo";
                        //tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else {

                    speakOK = false;

                    Log.e("error", "Initilization Failed!");
                }
            }
        });

    }




    static class BeaconView{
        public TextView name;
        public TextView distance;
    }

    LinearLayout mBeaconLayout;
    HashMap<Beacon, BeaconView> mBeacons;

    private void refreshBeacons(ArrayList<Beacon> beacons){
        HashMap<Beacon, BeaconView> newBeacons=new HashMap<>(beacons.size());
        Double min = Double.MAX_VALUE;
        if(!beacons.isEmpty()){
            Double temp=beacons.get(0).getDistance();
            if(temp<=1.0) {
                String id = beacons.get(0).getId3().toString();
                if (temp < min) {
                    min = temp;
                }

                    //this.area.setText(this.beaconToSector.get(id) + " min:" + min + " temp:" + beacons.get(0).getDistance());
                    this.areaName = this.beaconToSector.get(id);

            }else{
                //this.area.setText("Fuori range");
                this.areaName = "Fuori area";

            }

        }else{
            this.areaName = "Fuori area";

            //this.area.setText("Nessuna area trovata!");
        }

        /*for(Beacon b : beacons){
            BeaconView old= mBeacons.get(b);
            this.area.setText(b.getId3().toString());
            if(old==null){
                old=new BeaconView();
                old.name=new TextView(getApplicationContext());
                old.name.setTextColor(Color.rgb(0,0,0));
                old.name.setText("" + b.getId1() + " " + b.getId2() + " " + b.getId3());
                mBeaconLayout.addView(old.name);
                old.distance=new TextView(getApplicationContext());
                old.distance.setTextColor(Color.rgb(0,0,0));
                old.distance.setText(b.getDistance() + "metri");
                mBeaconLayout.addView(old.distance);
            }else{
                old.distance.setText(b.getDistance() + "metri");
                old.distance.invalidate();
            }
            newBeacons.put(b, old);
            mBeacons.remove(b);
        }
        */
        for(Map.Entry<Beacon, BeaconView> bv: mBeacons.entrySet()){
            mBeaconLayout.removeView(bv.getValue().name);
            mBeaconLayout.removeView(bv.getValue().distance);
        }

        mBeacons=newBeacons;

        //test.setText("[Beacons: " + beacons.size() + "] base: " + Math.random());
        //test.setTextColor(Color.rgb(0,0,0));
        //test.invalidate();
    }

    public Context getContext(){
        return getApplicationContext();
    }

    public void addListenerOnButtonArea() {
        this.areaButton = (Button)findViewById(R.id.buttonArea);

        this.areaButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(speakOK){
                    tts.speak(areaName, TextToSpeech.QUEUE_FLUSH, null);
                }

            }

        });

    }


    public void addListenerOnButtonCamera() {
        this.cameraButton = (Button)findViewById(R.id.Camerabutton);

        this.cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clicked on play links
                Intent playExampleIntent = null;

                playExampleIntent = new Intent(MainActivity.this.getContext(), RecognitionOnlyActivity.class);


                if (playExampleIntent != null) {
                    startActivity(playExampleIntent);
                    return;
                }

            }

        });

    }

    public void addListenerOnButtonHelp() {
        this.helpButton = (Button)findViewById(R.id.buttonHelp);

        this.helpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            //    if(speakOK){
            //        tts.speak(areaName, TextToSpeech.QUEUE_FLUSH, null);
            //    }

                tts.speak("Richiesta in corso, un addetto è in arrivo", TextToSpeech.QUEUE_FLUSH, null);

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                mHandler.sendMessage(mHandler.obtainMessage(0, beacons));
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

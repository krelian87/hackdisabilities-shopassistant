package backincode.org.shopassistant;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCameraView;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARCloudRecognitionError;
import com.craftar.CraftARImage;
import com.craftar.CraftARImageHandler;
import com.craftar.CraftARItem;
import com.craftar.CraftARResponseHandler;
import com.craftar.CraftARSDK;

public class RecognitionOnlyActivity extends CraftARActivity implements CraftARResponseHandler,CraftARImageHandler, OnClickListener {

    private final String TAG = "CraftARTrackingExample";
    private final static String COLLECTION_TOKEN="craftarexamples1";

    private View mScanningLayout;
    private View mTapToScanLayout;

    CraftARCamera mCamera;

    CraftARCloudRecognition mCloudRecognition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostCreate() {

        View mainLayout= (View) getLayoutInflater().inflate(R.layout.activity_recognition_only, null);
        CraftARCameraView cameraView = (CraftARCameraView) mainLayout.findViewById(R.id.camera_preview);
        super.setCameraView(cameraView);
        setContentView(mainLayout);

        mScanningLayout = findViewById(R.id.layout_scanning);
        mTapToScanLayout = findViewById(R.id.tap_to_scan);
        mTapToScanLayout.setClickable(true);
        mTapToScanLayout.setOnClickListener(this);

        //Initialize the SDK. From this SDK, you will be able to retrieve the necessary modules to use the SDK (camera, tracking, and cloud-recgnition)
        CraftARSDK.init(getApplicationContext(),this);

        //Get the camera to be able to do single-shot (if you just use finder-mode, this is not necessary)
        mCamera= CraftARSDK.getCamera();
        mCamera.setImageHandler(this); //Tell the camera who will receive the image after takePicture()

        //Setup cloud recognition
        mCloudRecognition= CraftARSDK.getCloudRecognition();//Obtain the cloud recognition module
        mCloudRecognition.setResponseHandler(this); //Tell the cloud recognition who will receive the responses from the cloud
        mCloudRecognition.setCollectionToken(COLLECTION_TOKEN); //Tell the cloud-recognition which token to use from the finder mode

        mCloudRecognition.connect(COLLECTION_TOKEN);

    }

    @Override
    public void searchCompleted(ArrayList<CraftARItem> results) {
        mScanningLayout.setVisibility(View.GONE);
        if(results.size()==0){
            Log.d(TAG,"Nothing found");
            Toast.makeText(getBaseContext(),"Oggetto non trovato!", Toast.LENGTH_SHORT).show();
        }else{
            CraftARItem item = results.get(0);
            if (!item.isAR()) {
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
                startActivity(launchBrowser);
                mTapToScanLayout.setVisibility(View.VISIBLE);
                mCamera.restartCameraPreview();
                return;
            }else{
                Toast.makeText(getBaseContext(),"Found item:"+item.getItemName(),Toast.LENGTH_SHORT).show();
            }
        }
        mTapToScanLayout.setVisibility(View.VISIBLE);
        mCamera.restartCameraPreview();
    }

    @Override
    public void connectCompleted(){
        Log.i(TAG,"Collection token is valid");
    }

    @Override
    public void requestFailedResponse(int requestCode,
                                      CraftARCloudRecognitionError responseError) {
        Log.d(TAG,"requestFailedResponse");
        Toast.makeText(getBaseContext(),"Oggetto non trovato!", Toast.LENGTH_SHORT).show();
        mScanningLayout.setVisibility(View.GONE);
        mTapToScanLayout.setVisibility(View.VISIBLE);
        mCamera.restartCameraPreview();

    }

    //Callback received for SINGLE-SHOT only (after takePicture).
    @Override
    public void requestImageReceived(CraftARImage image) {
        mCloudRecognition.searchWithImage(COLLECTION_TOKEN,image);
    }
    @Override
    public void requestImageError(String error) {
        //Take picture failed
        Toast.makeText(getBaseContext(),"Oggetto non trovato!", Toast.LENGTH_SHORT).show();
        mScanningLayout.setVisibility(View.GONE);
        mTapToScanLayout.setVisibility(View.VISIBLE);
        mCamera.restartCameraPreview();
    }

    @Override
    public void onClick(View v) {
        if (v == mTapToScanLayout) {
            mTapToScanLayout.setVisibility(View.GONE);
            mScanningLayout.setVisibility(View.VISIBLE);
            mCamera.takePicture();
        }
    }



}

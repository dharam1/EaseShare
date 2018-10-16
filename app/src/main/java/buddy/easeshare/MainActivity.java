package buddy.easeshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;

public class MainActivity extends AppCompatActivity {

    Button btnpic;
    ImageView imgTakenPic;
    private static final int CAM_REQUEST=1313;
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private CognitoUserPool cognitoUserPool;
    private CognitoUser cognitoUser;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnpic = (Button) findViewById(R.id.button);
        imgTakenPic = (ImageView)findViewById(R.id.imageView);

        btnpic.setOnClickListener(new btnTakePhotoClicker());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAM_REQUEST){
            bitmap = (Bitmap) data.getExtras().get("data");
            //Log.d("Hello D", test);
            //Log.d("Hello D",cognitoCachingCredentialsProvider.toString());
            cognitoUserPool = new CognitoConfig().userPool(getApplicationContext());
            cognitoUser = cognitoUserPool.getCurrentUser();
            new S3Upload().upload(cognitoUser,getApplicationContext(),bitmap);

            //imgTakenPic.setImageBitmap(bitmap);
        }
    }

    class btnTakePhotoClicker implements  Button.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,CAM_REQUEST);
        }
    }

    public void response(String result){
        //Log.d("Dharam",result);
        imgTakenPic.setImageBitmap(bitmap);
    }
}
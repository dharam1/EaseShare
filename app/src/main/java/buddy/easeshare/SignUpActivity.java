package buddy.easeshare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import java.util.LinkedHashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText eEmail , eContact , ePassword , eCode;
    private Button signup , verifycode;
    private CognitoUserPool userPool;
    private String username;
    private CognitoUser user;
    private LinkedHashMap<Integer, Character> numtochar = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        char x = 'a';

        for(int i=0;i<=9;i++){
            numtochar.put(i,x);
            //Log.d("Hello",String.valueOf(x));
            x++;
        }

        //eEmail = (EditText) findViewById(R.id.email);
        eContact = (EditText) findViewById(R.id.contact);
        ePassword = (EditText) findViewById(R.id.password);
        signup = (Button) findViewById(R.id.btn_signup);
        eCode = (EditText) findViewById(R.id.code);
        verifycode = (Button) findViewById(R.id.btn_verifycode);

        eCode.setVisibility(View.GONE);
        verifycode.setVisibility(View.GONE);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSuccess = signup(eContact.getText().toString(),ePassword.getText().toString());

            }
        });


    }

    private boolean signup(String contact,String password){
        userPool = new CognitoUserPool(getApplicationContext(), getResources().getString(R.string.userPoolId), getResources().getString(R.string.clientId), getResources().getString(R.string.clientSecret));
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        //userAttributes.addAttribute("email", email);
        userAttributes.addAttribute("phone_number", contact);

        for(int i = 0 ; i < contact.length() ; i++){
                username = username + String.valueOf(numtochar.get(Character.getNumericValue(contact.charAt(i))));
        }


        userPool.signUpInBackground(username, password, userAttributes, null, signupCallback);

        return false;
    }
    SignUpHandler signupCallback = new SignUpHandler() {

        @Override
        public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            if(!userConfirmed) {
                Log.d("Hello","Code Send");
                user = userPool.getUser(username);

                eCode.setVisibility(View.VISIBLE);
                verifycode.setVisibility(View.VISIBLE);

                //eEmail.setVisibility(View.GONE);
                eContact.setVisibility(View.GONE);
                ePassword.setVisibility(View.GONE);
                signup.setVisibility(View.GONE);

                verifycode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        user.confirmSignUpInBackground(eCode.getText().toString(), false,confirmationCallback);
                    }
                });


            }
            else {
                Log.d("Hello","User Confirmed");
            }
        }

        @Override
        public void onFailure(Exception exception) {
           Log.d("Hello","Signup Fail" + exception);
        }
    };

    GenericHandler confirmationCallback = new GenericHandler() {

        @Override
        public void onSuccess() {
            Log.d("Hello","Confirmed User Successfully");

        }

        @Override
        public void onFailure(Exception exception) {
            Log.d("Hello","Confirmation failure" +exception);
        }
    };

}

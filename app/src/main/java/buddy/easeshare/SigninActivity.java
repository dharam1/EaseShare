package buddy.easeshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

import java.util.LinkedHashMap;

public class SigninActivity extends AppCompatActivity  {

    private EditText eContact , ePassword;
    private Button signin;
    private CognitoUserPool cognitoUserPool;
    private CognitoUser cognitoUser;
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        eContact = (EditText) findViewById(R.id.input_contact);
        ePassword = (EditText) findViewById(R.id.input_password);
        signin = (Button) findViewById(R.id.btn_login);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signin(eContact.getText().toString(),ePassword.getText().toString());
            }
        });


        CognitoConfig cognitoConfig = new CognitoConfig();
        cognitoUserPool = cognitoConfig.userPool(getApplicationContext());

        cognitoUser = cognitoUserPool.getCurrentUser();

        if(cognitoUser != null){

            new CognitoCredentials().ifloggedin(cognitoUser,getApplicationContext());
            Intent gotoMain = new Intent(SigninActivity.this,MainActivity.class);
            startActivity(gotoMain);

        }
        else{
            Intent gotoSignin = new Intent(SigninActivity.this,SigninActivity.class);
            startActivity(gotoSignin);
             Log.d("Hello","Null");
        }

    }

    private void signin(String contact , String password){
        GenerateUsername generateUsername = new GenerateUsername();
        String username = generateUsername.generate(contact);

        CognitoConfig cognitoConfig = new CognitoConfig();
        cognitoUserPool = cognitoConfig.userPool(getApplicationContext());

        cognitoUser = cognitoUserPool.getUser(username);

        CognitoCredentials cognitoCredentials =new CognitoCredentials();
        boolean isSuccess = cognitoCredentials.ifnotloggedin(cognitoUser , password , contact);

        if(isSuccess){
                Intent gotoMain = new Intent(SigninActivity.this,MainActivity.class);
                startActivity(gotoMain);
        }
        else{
            Intent gotoSignin = new Intent(SigninActivity.this,SigninActivity.class);
            startActivity(gotoSignin);
        }
    }




}

package buddy.easeshare;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CognitoCredentials {



    private Regions regions = Regions.US_EAST_1;
    private CognitoUserSession cognitoUserSession;
    private String password , userId;
    private boolean isSuccess = false;
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private Context context;

    public boolean ifnotloggedin(CognitoUser cognitoUser , String password , String contact) {
        this.password = password;
        GenerateUsername generateUsername = new GenerateUsername();
        userId = generateUsername.generate(contact);

        cognitoUser.getSessionInBackground(handler);

        return isSuccess;
    }

    public void ifloggedin(CognitoUser cognitoUser, Context context) {
        this.context=context;
        cognitoUser.getSessionInBackground(handler);


        /**String idToken = cognitoUserSession.getIdToken().getJWTToken();

        cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(context, context.getResources().getString(R.string.identityPoolId) ,regions);

        Map<String, String> logins = new HashMap<String, String>();
        logins.put("cognito-idp.us-east-1.amazonaws.com/us-east-1_R0XOgTtZC", cognitoUserSession.getIdToken().getJWTToken());
        cognitoCachingCredentialsProvider.setLogins(logins);**/

    }

    AuthenticationHandler handler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Authentication was successful, the "userSession" will have the current valid tokens
            // Time to do awesome stuff
           cognitoUserSession = userSession;
           isSuccess = true;


           cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(context, context.getResources().getString(R.string.identityPoolId) ,regions);

           Map<String, String> logins = new HashMap<String, String>();
           logins.put("cognito-idp.us-east-1.amazonaws.com/us-east-1_R0XOgTtZC", cognitoUserSession.getIdToken().getJWTToken());
           cognitoCachingCredentialsProvider.setLogins(logins);


           //new MainActivity().pass(cognitoCachingCredentialsProvider);
            /**AmazonS3 s3 =new  AmazonS3Client(cognitoCachingCredentialsProvider);
            try {
                s3.deleteBucket("asehare-20181009212357-deployment");
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }**/
           //Log.d("Hello",cognitoCachingCredentialsProvider.getToken());

        }

        @Override
        public void getAuthenticationDetails(final AuthenticationContinuation continuation, final String userID) {
            AuthenticationDetails authDetails = new AuthenticationDetails(userId, password, null);
            continuation.setAuthenticationDetails(authDetails);
            continuation.continueTask();
        }

        @Override
        public void getMFACode(final MultiFactorAuthenticationContinuation continuation) {

        }

        @Override
        public void authenticationChallenge(final ChallengeContinuation continuation) {

        }

        @Override
        public void onFailure(final Exception exception) {
            Log.d("Hello","Sign in failed" +exception);
            isSuccess = false;
        }
    };


}

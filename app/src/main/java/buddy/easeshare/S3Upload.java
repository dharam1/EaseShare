package buddy.easeshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.util.HashMap;
import java.util.Map;

public class S3Upload {
    private Context context;
    private Bitmap bitmap;
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private CognitoUserSession cognitoUserSession;
    private Regions regions = Regions.US_EAST_1;

    public void upload(CognitoUser cognitoUser, Context context, Bitmap bitmap) {
        this.context=context;
        this.bitmap=bitmap;
        cognitoUser.getSessionInBackground(handler);
    }


    AuthenticationHandler handler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Authentication was successful, the "userSession" will have the current valid tokens
            // Time to do awesome stuff
            cognitoUserSession = userSession;



            cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(context, context.getResources().getString(R.string.identityPoolId) ,regions);

            Map<String, String> logins = new HashMap<String, String>();
            logins.put("cognito-idp.us-east-1.amazonaws.com/us-east-1_R0XOgTtZC", cognitoUserSession.getIdToken().getJWTToken());
            cognitoCachingCredentialsProvider.setLogins(logins);

            new Upload().execute(cognitoCachingCredentialsProvider);
            //new MainActivity().pass(cognitoCachingCredentialsProvider);
            /**AmazonS3 s3 =new  AmazonS3Client(cognitoCachingCredentialsProvider);
             try {
             s3.deleteBucket("asehare-20181009212357-deployment");
             } catch (AmazonServiceException e) {
             System.err.println(e.getErrorMessage());
             System.exit(1);
             }**/


        }

        @Override
        public void getAuthenticationDetails(final AuthenticationContinuation continuation, final String userID) {

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

        }
    };

    private class Upload extends AsyncTask<CognitoCachingCredentialsProvider, Void, String> {

        @Override
        protected String doInBackground(CognitoCachingCredentialsProvider... params) {
            AmazonS3 s3 =new AmazonS3Client(cognitoCachingCredentialsProvider);
            try {
                s3.deleteBucket("dharam-bhaumik");
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }
            return "Deleted";
        }

        @Override
        protected void onPostExecute(String result) {
                new MainActivity().response(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

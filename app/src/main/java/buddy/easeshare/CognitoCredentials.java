package buddy.easeshare;

import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

public class CognitoCredentials {




    private CognitoUserSession cognitoUserSession;
    private String password , userId;
    private boolean isSuccess = false;

    public boolean ifnotloggedin(CognitoUser cognitoUser , String password , String contact) {
        this.password = password;
        GenerateUsername generateUsername = new GenerateUsername();
        userId = generateUsername.generate(contact);

        cognitoUser.getSessionInBackground(handler);

        return isSuccess;
    }

    public CognitoUserSession ifloggedin(CognitoUser cognitoUser) {
        cognitoUser.getSessionInBackground(handler);

        return cognitoUserSession;
    }

    AuthenticationHandler handler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Authentication was successful, the "userSession" will have the current valid tokens
            // Time to do awesome stuff
           cognitoUserSession = userSession;
           isSuccess = true;

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

package buddy.easeshare;


import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;

public class CognitoConfig {
    private CognitoUserPool cognitoUserPool;

    public CognitoUserPool userPool(Context context){

        cognitoUserPool = new CognitoUserPool(context, context.getResources().getString(R.string.userPoolId), context.getResources().getString(R.string.clientId), context.getResources().getString(R.string.clientSecret));
        return cognitoUserPool;
    }

}

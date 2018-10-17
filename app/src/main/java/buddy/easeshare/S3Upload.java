package buddy.easeshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

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

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3Upload {
    private Context context;
    private Bitmap bitmap;
    private CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private CognitoUserSession cognitoUserSession;
    private Regions regions = Regions.US_EAST_1;
    private MainActivity mainActivity;

    public void upload(CognitoUser cognitoUser, Context context, Bitmap bitmap,MainActivity mainActivity) {
        this.context=context;
        this.bitmap=bitmap;
        this.mainActivity = mainActivity;

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


            AmazonRekognition rekognitionClient = new  AmazonRekognitionClient(cognitoCachingCredentialsProvider);
            String photo = "download.jpg";
            //ObjectMapper objectMapper = new ObjectMapper();

            // Get an image object from S3 bucket.
            Image image=new Image()
                    .withS3Object(new S3Object()
                            .withBucket("easeshare")
                            .withName(photo));

            // Search collection for faces similar to the largest face in the image.
            SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                    .withCollectionId("EaseShare")
                    .withImage(image)
                    .withFaceMatchThreshold(70F)
                    .withMaxFaces(2);

            SearchFacesByImageResult searchFacesByImageResult =
                    rekognitionClient.searchFacesByImage(searchFacesByImageRequest);

            System.out.println("Faces matching largest face in image from" + photo);
            List <FaceMatch> faceImageMatches = searchFacesByImageResult.getFaceMatches();
            for (FaceMatch face: faceImageMatches) {
                //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(face));
                Log.d("Hello",face.getFace().getBoundingBox().toString());
                System.out.println();
            }

            /**AmazonRekognition rekognitionClient = new  AmazonRekognitionClient(cognitoCachingCredentialsProvider);
            String photo = "Photo.JPG";

            Image image = new Image()
                    .withS3Object(new S3Object()
                            .withBucket("easeshare")
                            .withName("Photo.JPG"));
            Log.d("Hello",image.toString());

            IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                    .withImage(image)
                    .withCollectionId("EaseShare")
                    .withExternalImageId(photo)
                    .withDetectionAttributes("DEFAULT");
            IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest);

            System.out.println("Results for " + photo);
            System.out.println("Faces indexed:");
            List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
            for (FaceRecord faceRecord : faceRecords) {
                Log.d("  Face ID: " ,faceRecord.getFace().getFaceId());
                Log.d("  Location:" , faceRecord.getFaceDetail().getBoundingBox().toString());
            }**/



            /**AmazonRekognition rekognitionClient = new  AmazonRekognitionClient(cognitoCachingCredentialsProvider);

            String collectionId = "EaseShare";
            System.out.println("Creating collection: " + collectionId );

            CreateCollectionRequest request = new CreateCollectionRequest().withCollectionId(collectionId);

            CreateCollectionResult createCollectionResult = rekognitionClient.createCollection(request);
            Log.d("HEllo",createCollectionResult.getCollectionArn());
            //System.out.println("CollectionArn : " + createCollectionResult.getCollectionArn());
            //System.out.println("Status code : " + createCollectionResult.getStatusCode().toString());**/
            /**AmazonS3 s3 =new AmazonS3Client(cognitoCachingCredentialsProvider);
            try {
                s3.deleteBucket("dharam-bhaumik");
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }**/
            return "Deleted";
        }

        @Override
        protected void onPostExecute(String result) {
                ImageView imageView = mainActivity.findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
                new MainActivity().response(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

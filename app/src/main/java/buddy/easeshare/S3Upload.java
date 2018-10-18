package buddy.easeshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
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
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.squareup.picasso.Picasso;
//import com.bumptech.glide.Glide;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
    private URL url;
    ArrayList<String> list =new ArrayList<String>();


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

    private class Upload extends AsyncTask<CognitoCachingCredentialsProvider, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(CognitoCachingCredentialsProvider... params) {





            AmazonS3 s3 = new AmazonS3Client(cognitoCachingCredentialsProvider);
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest("easeshare", "p3.jpg")
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            url = s3.generatePresignedUrl(generatePresignedUrlRequest);
            list.add(url.toString());

            AmazonRekognition rekognitionClient = new  AmazonRekognitionClient(cognitoCachingCredentialsProvider);
            String photo = "p3.jpg";
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
                    .withMaxFaces(1);

            SearchFacesByImageResult searchFacesByImageResult =
                    rekognitionClient.searchFacesByImage(searchFacesByImageRequest);



            System.out.println("Faces matching largest face in image from" + photo);
            List <FaceMatch> faceImageMatches = searchFacesByImageResult.getFaceMatches();
            for (FaceMatch face: faceImageMatches) {
                //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(face));
                Log.d("Hello",face.getFace().getBoundingBox().toString());
                list.add(face.getFace().getBoundingBox().toString());
                System.out.println();
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                String temp = Base64.encodeToString(b, Base64.DEFAULT);
                list.add(temp);

            } catch (IOException e) {
                e.printStackTrace();
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
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList result) {
                ImageView imageView = mainActivity.findViewById(R.id.imageView);
                //ArrayList<String> res = new ArrayList<String>(result);
                String params = result.get(1).toString();
                String[] arr = params.split(",");
                float fwidth = Float.parseFloat(arr[0].split(":")[1].trim());
                float fheight = Float.parseFloat(arr[1].split(":")[1].trim());
                float fleft = Float.parseFloat(arr[2].split(":")[1].trim());
                float ftop = Float.parseFloat(arr[3].split(":")[1].trim().substring(0,7));





            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);

            byte[] encodeByte = Base64.decode(result.get(2).toString(), Base64.DEFAULT);
            Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

            int imgWidth = b.getWidth();
            int imgHeight = b.getHeight();

            Log.d("Hello",imgWidth + " :: " + imgHeight);
            Log.d("Hello",b.getWidth() + " :: " + b.getHeight());

            float top = ftop * (float) imgHeight;
            float left = fleft * (float) imgWidth;
            float height = fheight * (float) imgHeight;
            float width = fwidth * (float) imgWidth;

            Log.d("Hello", "top : "+ top + "left : " + left + "height : " + height + "width : " +width);

            Bitmap tBitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.RGB_565);

            Canvas tCanvas = new Canvas(tBitmap);

            Log.d("Hello",tCanvas.getWidth()+" :: " + tCanvas.getHeight());

            tCanvas.drawBitmap(b, 0, 0, null);

            tCanvas.drawRoundRect(new RectF(left,top,left+width,top+height), 2, 2, p);

            imageView.setImageDrawable(new BitmapDrawable(mainActivity.getResources(), tBitmap));


                //String url = result.get(0).toString();
                //Picasso.get().load(url).into(imageView);
                //imageView.setImageBitmap(bitmap);
                //new MainActivity().response(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

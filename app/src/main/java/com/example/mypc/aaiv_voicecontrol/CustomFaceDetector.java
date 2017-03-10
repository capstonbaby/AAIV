package com.example.mypc.aaiv_voicecontrol;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.example.mypc.aaiv_voicecontrol.Constants.FACE_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.OBJECT_RECOGNITION_MODE;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_FAILED;
import static com.example.mypc.aaiv_voicecontrol.Constants.PERSON_DETECTED_SUCCESSFULLY;
import static com.example.mypc.aaiv_voicecontrol.Constants.PersonGroupId;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_CONFIRMATION;
import static com.example.mypc.aaiv_voicecontrol.Constants.SPEECH_ONDONE_NOREQUEST;
import static com.example.mypc.aaiv_voicecontrol.Constants.VIEW_RECOGNITION_MODE;


/**
 * Created by 2TbP on 3/10/2017.
 */
public class CustomFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private SparseArray<Face> faces = new SparseArray<>();
    private int width = 0;
    private int heigh = 0;
    private byte[] arr = null;

    private Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));



    CustomFaceDetector(Detector<Face> delegate) {
        Log.i("STREAM", "create CustomFaceDector constructor");
        mDelegate = delegate;
    }

    @Override
    public SparseArray<Face> detect(Frame frame) {
        width = frame.getMetadata().getWidth();
        heigh = frame.getMetadata().getHeight();
        arr = frame.getGrayscaleImageData().array();
        synchronized (faces) {
            Log.i("STREAM", "detect function");
            faces = mDelegate.detect(frame);
            if (faces.size() > 0) {
                try {
                    Log.i("STREAM", "faces size: " + faces.size());

                    new test(frame).execute();
                    Thread.sleep(5000);

                    Log.i("STREAM", "after sleep");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
        return faces;
    }

    public class test extends AsyncTask<Void, Void, Void> {

        private Frame frame;

        public test(Frame frame) {
            this.frame = frame;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.i("TEST", "doInBackGround");


            arr = frame.getGrayscaleImageData().array();

            YuvImage yuvimage = new YuvImage(arr, ImageFormat.NV21, width, heigh, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, width, heigh), 100, baos); // Where 100 is the quality of the generated jpeg
            byte[] jpegArray = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
            savePicture(bitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("STREAM", "OnpostExecute");
        }
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    private void savePicture(Bitmap bm) {
        Matrix matrix = new Matrix();

        matrix.postRotate(270);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0, bm .getWidth(), bm .getHeight(), matrix, true);

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        Log.i("save picture", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            new Uploader(file).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Uploader extends AsyncTask<Void, Void, Map> {

        private File compressedFile;

        public Uploader(File compressedFile) {
            this.compressedFile = compressedFile;
        }

        @Override
        protected Map doInBackground(Void... params) {
            try {
                return cloudinary.uploader().upload(compressedFile, ObjectUtils.emptyMap());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map map) {
            if (map != null) {
                String url = (String) map.get("url");
                String imgUrl = url;

                new FaceDetection().execute(imgUrl);
            }
        }
    }

    private class FaceDetection extends AsyncTask<String, String, com.microsoft.projectoxford.face.contract.Face[]> {

        @Override
        protected com.microsoft.projectoxford.face.contract.Face[] doInBackground(String... params) {
            FaceServiceClient client = Constants.getmFaceServiceClient();
            Log.d("identify", "Detecting");
            try {

                return client.detect(
                        params[0],
                        true,
                        false,
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Gender
                        }
                );
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(com.microsoft.projectoxford.face.contract.Face[] faces) {
            if (faces != null) {
                List<UUID> faceids = new ArrayList<>();
                for (com.microsoft.projectoxford.face.contract.Face face : faces) {
                    faceids.add(face.faceId);
                }

                new FaceIdentify(PersonGroupId).execute(faceids.toArray(new UUID[faceids.size()]));

            } else {

            }
        }
    }

    private class FaceIdentify extends AsyncTask<UUID, Void, com.microsoft.projectoxford.face.contract.IdentifyResult[]> {

        String mPersonGroupId;

        public FaceIdentify(String mPersonGroupId) {
            this.mPersonGroupId = mPersonGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            Log.d("identify", "Identifying");

            FaceServiceClient client = Constants.getmFaceServiceClient();
            try {
                return client.identity(
                        this.mPersonGroupId,
                        params,
                        1
                );
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(com.microsoft.projectoxford.face.contract.IdentifyResult[] identifyResults) {
            if (identifyResults != null) {
                new PersonInfo(identifyResults).execute();
            }
        }
    }

    public class PersonInfo extends AsyncTask<Void, Void, Void> {

        String personIdentifyResultText = "";
        IdentifyResult[] identifyResults;

        public PersonInfo(IdentifyResult[] identifyResults) {
            this.identifyResults = identifyResults;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("identify", "Person");


            final FaceServiceClient client = Constants.getmFaceServiceClient();
            try {

                for (final com.microsoft.projectoxford.face.contract.IdentifyResult identifyResult :
                        identifyResults) {
                    if (identifyResult.candidates.size() > 0) {
                        final String personname = client.getPerson(PersonGroupId, identifyResult.candidates.get(0).personId).name;
                        personIdentifyResultText += personname + ", ";
                    }
                }
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.i("IDENTIFY", personIdentifyResultText);

        }

    }
}
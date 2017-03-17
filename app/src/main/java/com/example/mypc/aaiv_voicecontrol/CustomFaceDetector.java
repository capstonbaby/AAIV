package com.example.mypc.aaiv_voicecontrol;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
;
import java.util.UUID;

import rx.internal.util.unsafe.ConcurrentSequencedCircularArrayQueue;


/**
 * Created by 2TbP on 3/10/2017.
 */
public class CustomFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private Context mContext;
    private SparseArray<Face> faces = new SparseArray<>();
    private int width = 0;
    private int heigh = 0;
    private byte[] arr = null;
    private TextToSpeech mTextToSpeech;
    private TextView mtvResult;
    private ImageView mIvPreview;

    private MediaPlayer mp = null;

    private Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "debwqzo2g",
            "api_key", "852288139213848",
            "api_secret", "qsuCuMnpTZ11_WxuIuQ5kPZmdr4"));


    CustomFaceDetector(Detector<Face> delegate, Context context) {
        Log.i("STREAM", "create CustomFaceDector constructor");
        mDelegate = delegate;
        this.mContext = context;

        SetUpText2Speech();

        mtvResult = (TextView) ((Activity) mContext).findViewById(R.id.tv_stream_result);
        mIvPreview = (ImageView) ((Activity) mContext).findViewById(R.id.iv_stream_preview);
        mp = MediaPlayer.create(mContext, R.raw.camerasound);
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
                    new playSound().execute();
                    new Capture(frame).execute();
                    Thread.sleep(3000);

                    Log.i("STREAM", "after sleep");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
        return faces;
    }

    public class playSound extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mp.start();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class Capture extends AsyncTask<Void, Void, Void> {

        private Frame frame;

        public Capture(Frame frame) {
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
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        final File file = new File(myDir, fname);
        Log.i("save picture", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            mIvPreview.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mContext)
                            .load(file)
                            .into(mIvPreview);
                }
            });

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

                new FaceIdentify(Constants.getPopularPersonGroupId(), faceids.toArray(new UUID[faceids.size()])).execute();

            }
        }
    }

    private class FaceIdentify extends AsyncTask<Void, Void, com.microsoft.projectoxford.face.contract.IdentifyResult[]> {

        String mPersonGroupId;
        UUID[] mFaceIds;

        public FaceIdentify(String mPersonGroupId, UUID[] mFaceIds) {
            this.mPersonGroupId = mPersonGroupId;
            this.mFaceIds = mFaceIds;
        }

        @Override
        protected IdentifyResult[] doInBackground(Void... params) {
            Log.d("identify", "Identifying");

            FaceServiceClient client = Constants.getmFaceServiceClient();
            try {
                return client.identity(
                        this.mPersonGroupId,
                        mFaceIds,
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
                new PersonInfo(mPersonGroupId, identifyResults).execute();
            } else if (mPersonGroupId.equals(Constants.getPopularPersonGroupId())) {
                new FaceIdentify(Constants.getNormalPersonGroupId(), mFaceIds).execute();
            } else if (mPersonGroupId.equals(Constants.getNormalPersonGroupId())) {
                new FaceIdentify(Constants.getFreshPersonGroupId(), mFaceIds).execute();
            }
        }
    }

    public class PersonInfo extends AsyncTask<Void, Void, Void> {

        String personIdentifyResultText = "";
        String mPersonGroupId;
        IdentifyResult[] identifyResults;

        public PersonInfo(String mPersonGroupId, IdentifyResult[] identifyResults) {
            this.mPersonGroupId = mPersonGroupId;
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
                        final String personname = client.getPerson(mPersonGroupId, identifyResult.candidates.get(0).personId).name;
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
            mtvResult.post(new Runnable() {
                @Override
                public void run() {
                    mtvResult.setText(personIdentifyResultText);
                }
            });

            Speak(personIdentifyResultText);
        }

    }

    private void Speak(String text) {
        if (mTextToSpeech != null) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void SetUpText2Speech() {
        mTextToSpeech = new TextToSpeech(FaceTrackerActivity.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {

                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });

                    mTextToSpeech.setLanguage(new Locale("vi", "VN"));

                    Log.d("setupt2s", "Setup finished");
                } else if (status == TextToSpeech.ERROR) {
                    Toast.makeText(FaceTrackerActivity.getContext(), "Setup Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

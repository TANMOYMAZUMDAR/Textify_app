package com.example.textify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;
import java.util.Locale;



    public class MainActivity extends AppCompatActivity {


        private Button captureImageBtn,detectTextbtn;
        private ImageView imageView;
        private TextView  textView;
        Bitmap imageBitmap;
        private TextToSpeech mTTs;
        private Button mButton;
        private SeekBar seekBar;
        static final int REQUEST_IMAGE_CAPTURE = 1;
        private Button mTranslate;
        String say="";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);
            captureImageBtn=findViewById(R.id.image_capture);
            detectTextbtn=findViewById(R.id.image_detect);
            textView=findViewById(R.id.detect_text);
            imageView=findViewById(R.id.image_view);
           mButton=findViewById(R.id.button_speak);
           seekBar=findViewById(R.id.speed_seekBar);


            captureImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                       dispatchTakePictureIntent();
                       textView.setText("");
                }
            });

            detectTextbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detectTextFromImage();
                }
            });

            mTTs=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status==TextToSpeech.SUCCESS) {
                        int result = mTTs.setLanguage(Locale.ENGLISH);

                        if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
                            Log.e("TTS","Language not supported");
                        }else{
                            mButton.setEnabled(true);
                        }
                    }else{
                        Log.e("TTS","Intialization failed");
                    }
                }
            });

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speak();
                }
            });


        }

        @Override
        protected void onDestroy(){
            if(mTTs!=null)
            {
                mTTs.stop();
                mTTs.shutdown();
            }
            super.onDestroy();
        }

        private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode,resultCode,data);

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                 imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }
        }
        private void detectTextFromImage()
        {
            final FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionTextDetector firebaseVisionTextDetector= FirebaseVision.getInstance().getVisionTextDetector();
            firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    displayTextFromImage(firebaseVisionText);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d("Error",e.getMessage());
                }
            });
        }

        private void displayTextFromImage(FirebaseVisionText firebaseVisionText)
        {
            List<FirebaseVisionText.Block> blockList=firebaseVisionText.getBlocks();
            if(blockList.size()==0)
            {
                Toast.makeText(this,"No Text Found In The Image.",Toast.LENGTH_SHORT).show();
            }
            else
            {
                for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks())
                {
                    String text=block.getText();
                    say=say+text;
                    textView.setText(text);
                }

            }
        }
        private void speak(){
            mTTs.speak(say,TextToSpeech.QUEUE_FLUSH,null);
            float speed=(float)seekBar.getProgress() /50;
            if(speed<0.1)
                speed=0.1f;

            mTTs.setSpeechRate(speed);
        }







    }

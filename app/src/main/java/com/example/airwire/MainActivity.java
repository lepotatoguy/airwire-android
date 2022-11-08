package com.example.airwire;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.airwire.ml.AirwireV2;
import com.example.airwire.ml.V1AirwireV2;
import com.example.airwire.ml.V2AirwireV2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

//https://medium.com/analytics-vidhya/running-ml-models-in-android-using-tensorflow-lite-e549209287f0
public class MainActivity extends AppCompatActivity {

    Button selectBtn, predictBtn, captureBtn;
    TextView result;
    ImageView imageView;
    Bitmap bitmap; //for the prediction part

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permission for camera
        getPermission();

        selectBtn = findViewById(R.id.selectBtn);
        predictBtn = findViewById(R.id.predictBtn);
        captureBtn = findViewById(R.id.captureBtn);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        //on click of select image
        selectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
//                imageToBitMap(); // for testing purpose

            }
        });

        //on click of capture image
        captureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);

            }
        });


        //on click of predict
        predictBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try {
//                    AirwireV2 model = AirwireV2.newInstance(MainActivity.this);
//                    V1AirwireV2 model = V1AirwireV2.newInstance(MainActivity.this);
                    V2AirwireV2 model = V2AirwireV2.newInstance(MainActivity.this);
//                    Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.test_img);
//                    bitmap = Bitmap.createScaledBitmap(bitmap,240,400, true);
//                    imageView.setImageBitmap(bitmap);
                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 120, 200, 3}, DataType.FLOAT32);
                    // resizing bitmap image as per requirement

//                    Bitmap resizedBmp = Bitmap.createBitmap(bitmap, 0, 0, 240, 400); //https://stackoverflow.com/questions/15789049/crop-a-bitmap-image

//                    bitmap = Bitmap.createScaledBitmap(resizedBmp,240,400, true); // why 240 and 400: https://stackoverflow.com/questions/67720779/the-size-of-byte-buffer-and-the-shape-do-not-match-previously-not-answered
                    bitmap = Bitmap.createScaledBitmap(bitmap,240,400, true); // why 240 and 400: https://stackoverflow.com/questions/67720779/the-size-of-byte-buffer-and-the-shape-do-not-match-previously-not-answered




                    Log.d("inp_shape", inputFeature0.getBuffer().toString());
                    inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmap).getBuffer());
//                    Log.d("inp_shape", inputFeature0.getBuffer().toString());
                    // Runs model inference and gets result.
//                    AirwireV2.Outputs outputs = model.process(inputFeature0);
//                    V1AirwireV2.Outputs outputs = model.process(inputFeature0);
                    V2AirwireV2.Outputs outputs = model.process(inputFeature0);

                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

//                    Log.d("output-type", outputFeature0.getDataType().toString()); //float32
//                    float[] data=outputFeature0.getFloatArray();
                    int[] data=outputFeature0.getIntArray();
                    Log.d("output-type", Arrays.toString(data));
//                    Log.d("output-type", String.valueOf(outputFeature0.getFloatValue(0)));
                    Log.d("output-type", String.valueOf(outputFeature0.getIntValue(0)));

//                    result.setText(String.valueOf(outputFeature0.getFloatValue(0))+"");
                    result.setText(String.valueOf(outputFeature0.getIntValue(0))+"");
//                    result.setText(getMax(outputFeature0.getFloatArray())+"");

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
            }
        });
    }

    private void imageToBitMap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.test_img);
        bitmap = Bitmap.createScaledBitmap(bitmap,240,400, true);
        imageView.setImageBitmap(bitmap);
    }

    int getMax(float[] arr){
        int max = 0;
        for(int i =0;i<arr.length;i++){
            if(arr[i]>arr[max]) {
                max = i;
            }
        }
        return max;
    }

    void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 11);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==11){
            if(grantResults.length>0){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 10){ //user selected an image
            if(data!=null){
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==12){ //user captured the image
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
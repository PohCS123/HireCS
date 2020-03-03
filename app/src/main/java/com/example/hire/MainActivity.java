package com.example.hire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
/*import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;*/
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonCamera, buttonExtract, buttonDisplay;
    private ImageView imageViewResume;
    private TextView textViewExtractedText;
    private Bitmap imageBitmap;
    private BitmapDrawable imageBitmapDrawable;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentImagePath = null;
    Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    String camaraPermission[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCamera = findViewById(R.id.buttonCamera);
        buttonExtract = findViewById(R.id.buttonExtract);
        buttonDisplay = findViewById(R.id.buttonDisplay);
        imageViewResume = findViewById(R.id.imageViewResume);
        textViewExtractedText = findViewById(R.id.textViewExtractedText);

        camaraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //camaraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    dispatchTakePictureIntent();

                    textViewExtractedText.setText("");
                }

            }
        });

        buttonExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                detectTextFromImage();
            }
        });

        buttonDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImage();
            }
        });
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, camaraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void displayImage() {
        Intent intent = new Intent(this, DisplayImage.class);
        intent.putExtra("image_path", currentImagePath);
        startActivity(intent);
    }

    /*private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (imageFile != null) {
                //ContentValues values = new ContentValues();
                //values.put(MediaStore.Images.Media.TITLE,"NewPic");
                //values.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
                //imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }*/


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            ContentValues values = new ContentValues();
            Toast.makeText(MainActivity.this, "hi:", Toast.LENGTH_SHORT).show();
            values.put(MediaStore.Images.Media.TITLE, "NewPic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //Bundle extras = data.getExtras();
                //imageBitmap = (Bitmap) extras.get("data");
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
                //Toast.makeText(MainActivity.this,"Error 123:" ,Toast.LENGTH_SHORT).show();

                //imageBitmap = BitmapFactory.decodeFile(currentImagePath);
                //imageViewResume.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(MainActivity.this, "Error 111:", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();//get image uri
                imageViewResume.setImageURI(resultUri);
                imageBitmapDrawable = (BitmapDrawable) imageViewResume.getDrawable();
                imageBitmap = imageBitmapDrawable.getBitmap();
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(MainActivity.this, "Error :", Toast.LENGTH_SHORT).show();
        }

    }


    private void detectTextFromImage() {

        /*final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {

                displayTextFromImage(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error :", e.getMessage());
            }
        });*/

        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!recognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Error :", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock myItems = items.valueAt(i);
                sb.append(myItems.getValue());
                sb.append("\n");
            }
            /*Intent intent1 = new Intent(this,ExtractedText.class);
            Bundle bundle = new Bundle();
            bundle.putString("BundleText",sb.toString());
            intent1.putExtra("EXTRACTED_TEXT",bundle);
            startActivity(intent1);*/
            textViewExtractedText.setText(sb.toString());

        }

    }

    /*private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text Founded in this image", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                String text = block.getText();
                textViewExtractedText.setText(text);
            }
        }
    }*/

    private File getImageFile() throws Exception {

        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    Log.d("myTag","Camera:"+cameraAccepted+" Storage:"+writeStorageAccepted);
                    if(cameraAccepted && writeStorageAccepted){
                        dispatchTakePictureIntent();
                    }else{
                        Toast.makeText(MainActivity.this,"Permission Denied!" ,Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

}
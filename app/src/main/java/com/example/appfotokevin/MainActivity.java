package com.example.appfotokevin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CAMERA = 2;

    private ImageView imagePreview;
    private EditText editName, editDescription;
    private Button btnTakePhoto, btnSave;
    private RecyclerView imageList;

    private List<ImageData> imageDataList;
    private ImageListAdapter imageListAdapter;

    private DatabaseHelper databaseHelper;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);
        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSave = findViewById(R.id.btnSave);
        imageList = findViewById(R.id.imageList);

        databaseHelper = new DatabaseHelper(this);

        imageDataList = new ArrayList<>();
        imageListAdapter = new ImageListAdapter(this, imageDataList);
        imageList.setLayoutManager(new LinearLayoutManager(this));
        imageList.setAdapter(imageListAdapter);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_PERMISSION_CAMERA);
                } else {
                    openCamera();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageData();
                loadImagesFromDatabase();
                clearFields();
            }
        });

        loadImagesFromDatabase();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Image");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            displayImagePreview(imageUri);
        }
    }

    private void displayImagePreview(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imagePreview.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveImageData() {
        String name = editName.getText().toString();
        String description = editDescription.getText().toString();
        String imagePath = imageUri.toString();

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_IMAGE_PATH, imagePath);

        long newRowId = db.insert(DatabaseHelper.TABLE_IMAGES, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void loadImagesFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_DESCRIPTION,
                DatabaseHelper.COLUMN_IMAGE_PATH
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_IMAGES, projection, null, null, null, null, null);

        imageDataList.clear();

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME);
            int descriptionIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION);
            int imagePathIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH);

            int id = cursor.getInt(idIndex);
            String name = cursor.getString(nameIndex);
            String description = cursor.getString(descriptionIndex);
            String imagePath = cursor.getString(imagePathIndex);

            ImageData imageData = new ImageData(id, name, description, imagePath);
            imageDataList.add(imageData);
        }

        cursor.close();
        db.close();

        imageListAdapter.notifyDataSetChanged();
    }

    private void clearFields() {
        editName.setText("");
        editDescription.setText("");
        imagePreview.setImageResource(android.R.color.transparent);
    }
}

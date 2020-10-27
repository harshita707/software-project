package com.harshita.imagestenographyapp.activities.encrypt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.harshita.imagestenographyapp.algorithms.AESencryption;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.harshita.imagestenographyapp.R;
import com.harshita.imagestenographyapp.activities.stego.StegoActivity;
import com.harshita.imagestenographyapp.utils.Constants;
import com.harshita.imagestenographyapp.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EncryptActivity extends AppCompatActivity implements EncryptView {

  @BindView(R.id.etSecretMessage)
  EditText etSecretMessage;
  @BindView(R.id.etSecretKey)
  EditText etSecretKey;
  @BindView(R.id.ivCoverImage)
  ImageView ivCoverImage;
  @BindView(R.id.ivSecretImage)
  ImageView ivSecretImage;

  /*static final int REQUEST_IMAGE_CAPTURE = 1;
  String currentPhotoPath;*/
  static final int REQUEST_TAKE_PHOTO = 1;

  static final int CAMERA_REQUEST = 1888;
  static final int MY_CAMERA_PERMISSION_CODE = 100;

  @OnClick({R.id.ivCoverImage, R.id.ivSecretImage})
  public void onCoverSecretImageClick(View view) {

    final CharSequence[] items = {
      getString(R.string.select_image_dialog)
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(EncryptActivity.this);
    builder.setTitle(getString(R.string.select_image_title));
    builder.setCancelable(false);
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int item) {
        if (items[item].equals(getString(R.string.take_image_dialog))) {

          if (ContextCompat.checkSelfPermission(getApplicationContext(),
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getApplicationContext(),
              Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(EncryptActivity.this,
              new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
              Constants.PERMISSIONS_CAMERA);

          } else {
            openCamera();
          }
        } else if (items[item].equals(getString(R.string.select_image_dialog))) {

          if (ContextCompat.checkSelfPermission(getApplicationContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(EncryptActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              Constants.PERMISSIONS_EXTERNAL_STORAGE);

          } else {
            chooseImage();
          }
        }
      }
    });

    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });

    if (view.getId() == R.id.ivCoverImage) {
      whichImage = Constants.COVER_IMAGE;
    } else if (view.getId() == R.id.ivSecretImage) {
      whichImage = Constants.SECRET_IMAGE;
    }

    builder.show();
  }

  @OnClick(R.id.bEncrypt)
  public void onButtonClick() {
    if (secretMessageType == Constants.TYPE_IMAGE) {
      mPresenter.encryptImage();
    } else if (secretMessageType == Constants.TYPE_TEXT) {
      String text = getSecretMessage();

      if (!text.isEmpty()) {
        mPresenter.encryptText();
      } else {
        showToast(R.string.secret_text_empty);
      }
    }
  }

  private ProgressDialog progressDialog;
  private EncryptPresenter mPresenter;
  private int whichImage = -1;
  private int secretMessageType = Constants.TYPE_TEXT;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_encrypt_text);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Encrypt Text into Image");

/*    ivSecretImage.setVisibility(View.GONE);
    secretMessageType = Constants.TYPE_TEXT;*/

    ButterKnife.bind(this);

    //initToolbar();

    progressDialog = new ProgressDialog(EncryptActivity.this);
    progressDialog.setMessage("Please wait...");

    mPresenter = new EncryptPresenterImpl(this);

    SharedPreferences sp = getSharedPrefs();
    String filePath = sp.getString(Constants.PREF_COVER_PATH, "");
    boolean isCoverSet = sp.getBoolean(Constants.PREF_COVER_IS_SET, false);

    if (isCoverSet) {
      setCoverImage(new File(filePath));
    }
  }

/*  @Override
  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Encryption");
    }
  }*/

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == MY_CAMERA_PERMISSION_CODE)
    {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
      }
      else
      {
        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
      }
    }
    /*switch (requestCode) {
      case Constants.PERMISSIONS_CAMERA:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
          grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          openCamera();
        }
        break;
      case Constants.PERMISSIONS_EXTERNAL_STORAGE:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          chooseImage();
        }
        break;
    }*/
  }

  @Override
  public void openCamera() {

    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
    {
      requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
    }
    else
    {
      Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
      startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (intent.resolveActivity(getPackageManager()) != null) {
      File photoFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.png");

      // Continue only if the File was successfully created
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
      }
    /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File file = new File(android.os.Environment
      .getExternalStorageDirectory(), "temp.png");

    Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", file);

    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    startActivityForResult(intent, Constants.REQUEST_CAMERA);*/
    }
    }


    /*public File createImageFile()throws IOException {
      // Create an image file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      File image = File.createTempFile(
              imageFileName,  // prefix
              ".jpg",         // suffix
              storageDir      // directory
      );

      // Save a file: path for use with ACTION_VIEW intents
      currentPhotoPath = image.getAbsolutePath();
      return image;
    }*/

  @Override
  public void chooseImage() {
    Intent intent = new Intent(
      Intent.ACTION_PICK,
      android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    startActivityForResult(
      Intent.createChooser(intent, getString(R.string.choose_image)),
      Constants.SELECT_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      if (requestCode == Constants.REQUEST_CAMERA) {
        mPresenter.selectImageCamera(whichImage);
      } else if (requestCode == Constants.SELECT_FILE) {
        Uri selectedImageUri = data.getData();
        String tempPath = getPath(selectedImageUri, EncryptActivity.this);
        mPresenter.selectImage(whichImage, tempPath);
      }
    }
  }


  public String getPath(Uri uri, AppCompatActivity activity) {
    String[] projection = {MediaStore.MediaColumns.DATA};
    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }

  @Override
  public void startStegoActivity(String filePath) {
    Intent intent = new Intent(EncryptActivity.this, StegoActivity.class);
    intent.putExtra(Constants.EXTRA_STEGO_IMAGE_PATH, filePath);
    startActivity(intent);
  }

  @Override
  public Bitmap getCoverImage() {
    return ((BitmapDrawable) ivCoverImage.getDrawable()).getBitmap();
  }

  @Override
  public void setCoverImage(File file) {
    showProgressDialog();
    Picasso.with(this)
      .load(file)
      .fit()
      .placeholder(R.drawable.ic_upload_icon)
      .into(ivCoverImage);
    stopProgressDialog();
    whichImage = -1;

    SharedPreferences.Editor editor = getSharedPrefs().edit();
    editor.putString(Constants.PREF_COVER_PATH, file.getAbsolutePath());
    editor.putBoolean(Constants.PREF_COVER_IS_SET, true);
    editor.apply();
  }

  @Override
  public Bitmap getSecretImage() {
    return ((BitmapDrawable) ivSecretImage.getDrawable()).getBitmap();
  }

  @Override
  public void setSecretImage(File file) {
    showProgressDialog();
    Picasso.with(this)
      .load(file)
      .fit()
      .placeholder(R.drawable.ic_upload_icon)
      .into(ivSecretImage);
    stopProgressDialog();
    whichImage = -1;
  }

  @Override
  public String getSecretMessage() {

    String inputText = etSecretMessage.getText().toString().trim();
    String key = etSecretKey.getText().toString().trim();
    String outputText = null;
    try {
      outputText = AESencryption.encryptText(inputText, key);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return outputText;
  }

  @Override
  public void setSecretMessage(String secretMessage) {
    etSecretMessage.setText(secretMessage);
  }

  @Override
  public void showToast(int message) {
    StandardMethods.showToast(this, message);
  }

  @Override
  public void showProgressDialog() {
    if (progressDialog != null && !progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override
  public void stopProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public SharedPreferences getSharedPrefs() {
    return getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
  }
}

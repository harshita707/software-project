package com.harshita.imagestenographyapp.activities.decrypt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.harshita.imagestenographyapp.algorithms.AESencryption;
import com.squareup.picasso.Picasso;

import java.io.File;

import com.harshita.imagestenographyapp.R;
import com.harshita.imagestenographyapp.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DecryptResultActivity extends AppCompatActivity {

  @BindView(R.id.tvSecretMessage)
  TextView tvSecretMessage;

  @BindView(R.id.ivSecretImage)
  ImageView ivSecretImage;


  private String secretImagePath;
  private String secretMessage;
  private String password;
  private String decryptText = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decrypt_result);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Decrypted Result Image");

    ButterKnife.bind(this);

    //initToolbar();

    Intent intent = getIntent();

    if (intent != null) {
      password = intent.getStringExtra("secretKey");
      Bundle bundle = intent.getExtras();
      secretMessage = bundle.getString(Constants.EXTRA_SECRET_TEXT_RESULT);
      secretImagePath = bundle.getString(Constants.EXTRA_SECRET_IMAGE_RESULT);
    }

    if (secretMessage != null) {
      try {
         decryptText = AESencryption.decryptText(secretMessage, password);
      } catch (Exception e) {
        Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
        e.printStackTrace();
      }
      tvSecretMessage.setText(decryptText);
    } else if (secretImagePath != null) {
      ivSecretImage.setVisibility(View.VISIBLE);
      setSecretImage(secretImagePath);
    }
  }

/*  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Decryption");
    }
  }*/

  public void setSecretImage(String path) {
    Picasso.with(this)
      .load(new File(path))
      .fit()
      .placeholder(R.drawable.ic_upload_icon)
      .into(ivSecretImage);
  }
}

package com.harshita.imagestenographyapp.activities.decrypt;


interface DecryptPresenter {

  void setStegiImgPath(String path);

  void selectImage(String path);


  void decryptMessage();
}

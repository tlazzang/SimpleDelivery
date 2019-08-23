package com.example.shim.simpledeliverydeliverer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class MyProfileFragment extends Fragment {
    private final String[] permissionArray= {android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int PICK_FROM_ALBUM = 100;
    private ImageView iv_profile;
    private Uri imgUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        AWSMobileClient.getInstance().initialize(getActivity()).execute();
        initView(view);
        getPermission(permissionArray);
        return view;
    }

    public void initView(View view){
        iv_profile = (ImageView) view.findViewById(R.id.myProfilePrag_iv_img);

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhotoFromAlbum();
            }
        });
    }

    public void getPermission(String[] permissionArray){
        for(String permission : permissionArray){
            if(ContextCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, 100);
            }
        }
    }


    public void pickPhotoFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    public void uploadWithTransferUtility() {
        final String KEY = "YOUR_KEY";
        final String SECRET = "YOUR_SECRET";

        BasicAWSCredentials credentials = new BasicAWSCredentials(KEY, SECRET);
        AmazonS3Client s3Client = new AmazonS3Client(credentials);

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getActivity())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(s3Client)
                        .build();

        // "jsaS3" will be the folder that contains the file
        String fileName = "test.jpg";
        final File file = new File(getRealPathFromURI(imgUri));
        TransferObserver uploadObserver =
                transferUtility.upload("userPhoto/" + file.getName(), file);


        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed download.
                    String imgUrl = "https://simpledelivery-userfiles-mobilehub-895254981.s3.ap-northeast-2.amazonaws.com/userPhoto/"+ file.getName();
                    Log.d("MyProfileFragment", "upload success");
                    Log.d("MyProfileFragment", imgUrl);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.d("MyProfileFragment", ex.getMessage());
            }

        });

// If your upload does not trigger the onStateChanged method inside your
// TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_FROM_ALBUM && resultCode==RESULT_OK)
        {
            imgUri = data.getData();
            Log.d("MyProfileFragment", getRealPathFromURI(imgUri));


            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //이미지가 한계이상(?) 크면 불러 오지 못하므로 사이즈를 줄여 준다.
            int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);


            iv_profile.setImageBitmap(scaled);

            uploadWithTransferUtility();
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

}

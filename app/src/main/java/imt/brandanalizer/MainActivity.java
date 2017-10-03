package imt.brandanalizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.CanvasFrame;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.support.v4.content.FileProvider.getUriForFile;
import static java.io.File.createTempFile;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {
    Boolean first;
    Context context;
    private static final int CAMERA_PIC_REQUEST = 001;
    String mCurrentPhotoPath;

    private LinearLayout mGallery;
    private int[] mImgIds;
    private LayoutInflater mInflater;
    private HorizontalScrollView horizontalScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
  //      mInflater = LayoutInflater.from(this);
   //     initData();
   //     initView();
        if (savedInstanceState!=null && savedInstanceState.containsKey("picture")) {
            first=false;
            mCurrentPhotoPath=savedInstanceState.getString("picture");
            ImageView image = (ImageView) findViewById(R.id.imageView1);
            image.setImageURI(Uri.parse(mCurrentPhotoPath));
            image.setImageMatrix(takeMatrix(image));
  //          image.setScaleType(ImageView.ScaleType.MATRIX);
            image.invalidate();
        }
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(ACTION_IMAGE_CAPTURE);
            first=true;
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.d("PERMISSION", e.getMessage());
                }
                if (photoFile != null) {
                    Uri photoURI = getUriForFile(this,
                            "com.brandanalizer.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);

    /*                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);*/
                }
            }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);//getFilesDir();
        File image = createTempFile(imageFileName,".jpg",storageDir);
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" +image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST){
            ImageView image = (ImageView) findViewById(R.id.imageView1);
            image.setImageURI(Uri.parse(mCurrentPhotoPath));
            image.setImageMatrix(takeMatrix(image));
  //          image.setScaleType(ImageView.ScaleType.MATRIX);
            image.invalidate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString("picture",mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    private Matrix takeMatrix(ImageView image){
        Matrix matrix = new Matrix(image.getImageMatrix());
        ExifInterface exifReader = null;
        try {
            exifReader = new ExifInterface(mCurrentPhotoPath.substring(5));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        if (orientation ==ExifInterface.ORIENTATION_NORMAL) {
// Do nothing. The original image is fine.
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90,image.getDrawable().getBounds().width()/5,image.getDrawable().getBounds().height()/5);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180,image.getDrawable().getBounds().width()/2,image.getDrawable().getBounds().height()/2);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                   matrix.postRotate(270,image.getDrawable().getBounds().width()/2,image.getDrawable().getBounds().height()/2);
        }
        if (first==true) {
            int Height = image.getHeight();
            int Width = image.getWidth();
            int newHeight = 300;
            int newWidth = 300;
            Drawable drawable = image.getDrawable();
            Rect rectDrawable = drawable.getBounds();
            float leftOffset = (image.getMeasuredWidth() - rectDrawable.width()) / 2f;
            float topOffset = (image.getMeasuredHeight() - rectDrawable.height()) / 2f;
            float scaleHeight = ((float) newHeight / Height);
            float scaleWidth = ((float) newWidth / Width);
            float minScale = Math.min(scaleHeight, scaleWidth);

            // tx, ty should be the translation to take the image back to the screen center
            float tx = Math.max(0,
                    0.5f * ((float) image.getMeasuredWidth() - (minScale * rectDrawable.width())));
            float ty = Math.max(0,
                    0.5f * ((float) image.getMeasuredHeight() - (minScale * rectDrawable.height())));
            matrix.postScale(scaleWidth, scaleHeight);
            matrix.postTranslate(tx, ty);
        }
        first=false;
        return matrix;
    }

    private void initData()
    {
        mImgIds = new int[] { R.drawable.logomacdo,R.drawable.logostarbuck};
    }

/*    private void initView()
    {
        mGallery = (LinearLayout) findViewById(R.id.id_gallery);

        for (int i = 0; i < mImgIds.length; i++)
        {

            View view = mInflater.inflate(R.layout.activity_gallery_item,
                    mGallery, false);
            ImageView img = (ImageView) view
                    .findViewById(R.id.id_index_gallery_item_image);
            img.setImageResource(mImgIds[i]);
            TextView txt = (TextView) view
                    .findViewById(R.id.id_index_gallery_item_text);
            txt.setText("info "+i);
            mGallery.addView(view);
        }
    }*/
}
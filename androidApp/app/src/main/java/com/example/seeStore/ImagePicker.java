package com.example.seeStore;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;

import androidx.annotation.ColorInt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 Ref: https://gist.github.com/Mariovc/f06e70ebe8ca52fbbbe2
 I have modified this code to suit this application needs: Resize the bitmap, get the RGB values from bitmap, return the byte array
 */
public class ImagePicker {
    private static final String TEMP_IMAGE_NAME = "tempImage";
    private static final String TAG = "Image Picker";

    public static Intent getPickImageIntent(Context context) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        // intents for CAMERA and GALLERY
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        // choose intent
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);
        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),"Chọn hình ảnh từ ...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public static byte[] getImageFromResult(Context context, Intent imageReturnedIntent) {
        Bitmap bm = null;
        //File imageFile = getTempFile(context);
        boolean isCamera = (imageReturnedIntent == null || imageReturnedIntent.getData() == null);
        Uri selectedImage;
        if (isCamera) {     /** CAMERA **/
            bm = (Bitmap) imageReturnedIntent.getExtras().get("data");
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bm, "Image123", null);
            selectedImage = Uri.parse(path);
        } else {            /** ALBUM **/
            selectedImage = imageReturnedIntent.getData();
        }

        bm = getImageResized(context, selectedImage);
        int rotation = getRotation(context, selectedImage, isCamera);
        bm = rotate(bm, rotation);
        byte[] rbgValues = getRgbValuesFromBitmap(bm);
        return rbgValues;
    }

    private static byte[] getRgbValuesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap targetBmp = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        ColorMatrix colorMatrix = new ColorMatrix();
        ColorFilter colorFilter = new ColorMatrixColorFilter(
                colorMatrix);
        Bitmap argbBitmap = Bitmap.createBitmap(targetBmp.getWidth(), targetBmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(argbBitmap);
        Paint paint = new Paint();

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(targetBmp, 0, 0, paint);

        int width = targetBmp.getWidth();
        int height = targetBmp.getHeight();
        int componentsPerPixel = 3;
        int totalPixels = width * height;
        int totalBytes = totalPixels * componentsPerPixel;

        byte[] rgbValues = new byte[totalBytes];
        @ColorInt int[] argbPixels = new int[totalPixels];
        argbBitmap.getPixels(argbPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < totalPixels; i++) {
            @ColorInt int argbPixel = argbPixels[i];
            int red = Color.red(argbPixel);
            int green = Color.green(argbPixel);
            int blue = Color.blue(argbPixel);
            rgbValues[i * componentsPerPixel + 0] = (byte) red;
            rgbValues[i * componentsPerPixel + 1] = (byte) green;
            rgbValues[i * componentsPerPixel + 2] = (byte) blue;
        }

        return rgbValues;
    }


//    private static File getTempFile(Context context) {
//        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
//        imageFile.getParentFile().mkdirs();
//        return imageFile;
//    }
    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            if(Build.VERSION.SDK_INT < 28) {
                bm = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, selectedImage);
                bm = ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bm = resizeBitmap(bm, 256, 256);
        return bm;
    }

    private static Bitmap resizeBitmap(Bitmap image, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
        return scaledBitmap;
    }

    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return bmOut;
        }
        return bm;
    }
}

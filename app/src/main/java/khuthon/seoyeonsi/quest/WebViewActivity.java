package khuthon.seoyeonsi.quest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";


    final String locationSendUrl = "https://front.seoyeonsi.bu.to/quest_get.php";

    Button button;
    private GpsTracker gpsTracker;

    // WebView Variables
    private WebView webView;
    private String url = "https://front.seoyeonsi.bu.to/quest_get.php";
    // private String url = "https://www.naver.com";


    // WebView File Upload Variables
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    public MessageHandler messageHandler = new MessageHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        button = (Button) findViewById(R.id.button);

        // 아래부터 WebView 처리
        webView = (WebView) findViewById(R.id.webView);

        Log.d(TAG, webView.toString());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            /*
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                final String need_url = "https://front.seoyeonsi.bu.to/quest_get.php";
                if (need_url.equalsIgnoreCase(url)) {
                    gpsTracker = new GpsTracker(WebViewActivity.this);

                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    String address = getCurrentAddress(latitude, longitude);



                    Toast.makeText(WebViewActivity.this, "주소\n" + address +
                            "\n현재위치\n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();



                    String script = "javascripot:change_location(" + latitude + ", " + longitude + ", " + address + ")";
                    view.loadUrl(script);

                }

                Log.d(TAG, url);

                return true;
            }

             */

            @Override
            public void onPageFinished(WebView view, String url) {

                // **** Example : 네이티브와 웹뷰 간 통신 스크립트 이용 ****
                // 여기서 WebView의 데이터를 가져오는 작업을 한다.

                /*if (url.equals(url)) {
                    String keyword = "Temp";

                    String script = "javascript:function afterLoad() {"
                            + "document.getElementById('keyword').value = '" + keyword + "';"
                            + "};"
                            + "afterLoad();";

                    view.loadUrl(script);
                }*/

                final String need_url = "https://front.seoyeonsi.bu.to/quest_get.php";

                Log.d(TAG, "현재 URL : " + url);

            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            // Alert 처리
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            // Confirm 처리
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("Yes",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("No",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadFile, WebChromeClient.FileChooserParams fileChooserParams) {
                if(mFilePathCallback !=null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }

                mFilePathCallback = uploadFile;
                Intent i =new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                startActivityForResult(Intent.createChooser(i, "File Chooser"), INPUT_FILE_REQUEST_CODE);

                return true;

            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(getClass().getName(), "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(TYPE_IMAGE);

                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
        });


        webView.addJavascriptInterface(new WebAppInterface(this), "nativeApp");

        webView.loadUrl(url);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(WebViewActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);

                Toast.makeText(WebViewActivity.this, "주소\n" + address +
                        "\n현재위치\n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + "001" + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INPUT_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mFilePathCallback == null) {
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                    Uri[] results = new Uri[]{getResultUri(data)};

                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                } else {
                    if (mUploadMessage == null) {
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                    Uri result = getResultUri(data);

                    Log.d(getClass().getName(), "openFileChooser : " + result);
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
            } else {
                if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
                if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
                mFilePathCallback = null;
                mUploadMessage = null;
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                //filePath = "file:" + filePath.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        if (webView.getOriginalUrl().equalsIgnoreCase(url)) {
            super.onBackPressed();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // GPS 및 주소 관련 코드
    public String getCurrentAddress(double latitude, double longitude) {
        // 지오코더 => GPS를 주소로 변환

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            // 네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용 불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용 불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onLoad() {
            gpsTracker = new GpsTracker(WebViewActivity.this);

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            String address = getCurrentAddress(latitude, longitude);



            //Toast.makeText(WebViewActivity.this, "주소\n" + address +
             //       "\n현재위치\n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

            String script = "javascript:change_location(" + latitude + ", " + longitude + ", '" + address + "');";

            Message message = messageHandler.obtainMessage();
            message.what = 1;
            message.obj = script;

            messageHandler.sendMessage(message);

            Log.d(TAG, "현재 웹뷰의 url : " + webView.getUrl());
            /*
            if (webView.getUrl().equalsIgnoreCase(locationSendUrl)) {


                Log.d(TAG, "IF문 안으로 들어옴");
            }*/
        }
    }

    class MessageHandler extends Handler {
        public void handleMessage(Message msg) {
            webView.loadUrl(msg.obj.toString());
            Log.d(TAG, "obj : " + msg.obj.toString());
            Log.d(TAG, "MessageHandler");
        }
    }
}

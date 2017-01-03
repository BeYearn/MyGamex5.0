package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.utils.ThreadManager;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2016/12/13.
 */

public class QQShareUtils {
    private Tencent mTencent;
    private Context mContext;
    private static QQShareUtils mIntance;
    public static int Build_VERSION_KITKAT = 19;
    public static String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
    private static final String PATH_DOCUMENT = "document";
    private String ImageUrl = "http://img7.doubanio.com/lpic/s3635685.jpg";
    private EmaSDKListener mListener;

    public static QQShareUtils getIntance(Context context) {
        if (mIntance == null) {
            mIntance = new QQShareUtils(context);
        }
        return mIntance;
    }

    private QQShareUtils(Context context) {
        this.mContext = context;
        mTencent = Tencent.createInstance(ConfigManager.getInstance(mContext).getQQAppId(), mContext);
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
        final Bundle params = new Bundle();
        //  params.putString(QQShare.SHARE_TO_QQ_APP_NAME,appName);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, ImageUrl);

        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ((Activity) mContext, params, emIUiListener);
                }
            }
        });
    }

    public void shareQQFriendImage(EmaSDKListener listener, Bitmap bitmap) {
        this.mListener = listener;
        // startPickLocaleImage();

        saveBitmap(bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                //  params.putString(QQShare.SHARE_TO_QQ_APP_NAME,appName);
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, ImageUrl);

                ThreadManager.getMainHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        if (null != mTencent) {
                            mTencent.shareToQQ((Activity) mContext, params, emIUiListener);
                        }
                    }
                });
            }
        });
        /*try {
            saveBitmap(bitmap,mContext);
            final Bundle params = new Bundle();
            //  params.putString(QQShare.SHARE_TO_QQ_APP_NAME,appName);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, ImageUrl);

            ThreadManager.getMainHandler().post(new Runnable() {

                @Override
                public void run() {
                    if (null != mTencent) {
                        mTencent.shareToQQ((Activity) mContext, params, emIUiListener);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //  params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);

    }

    public void shareQzoneImage(EmaSDKListener listener, Bitmap bitmap) {
        this.mListener = listener;
        // startPickLocaleImage();

        saveBitmap(bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, 3);
                ArrayList<String> imageUrls = new ArrayList<String>();
                imageUrls.add(ImageUrl);
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
                ThreadManager.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mTencent) {
                            mTencent.publishToQzone((Activity) mContext, params, emIUiListener);
                        }
                    }
                });
            }
        });
        /*try {
            saveBitmap(bitmap,mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, 3);
            ArrayList<String> imageUrls = new ArrayList<String>();
            imageUrls.add(ImageUrl);
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);

            ThreadManager.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (null != mTencent) {
                        mTencent.publishToQzone((Activity) mContext, params, emIUiListener);
                    }
                }
            });*/
        //  params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);

    }

    public void shareQQFriendsWebPage(EmaSDKListener listener, final String title, final String url, final String summary, Bitmap bitmap /*String imageUrl*/) {
        this.mListener = listener;
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(summary) || bitmap == null ||/*TextUtils.isEmpty(imageUrl)||*/TextUtils.isEmpty(url)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBitmap(bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, ImageUrl);
                // params.putString(QQShare.SHARE_TO_QQ_APP_NAME,appName);
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                //  params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);
                ThreadManager.getMainHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        if (null != mTencent) {
                            mTencent.shareToQQ((Activity) mContext, params, emIUiListener);
                        }
                    }
                });
            }
        });
      /*  try {
            saveBitmap(bitmap,mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, ImageUrl);
       // params.putString(QQShare.SHARE_TO_QQ_APP_NAME,appName);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
      //  params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ((Activity) mContext, params, emIUiListener);
                }
            }
        });*/
    }

    public void shareQzoneWebPage(EmaSDKListener listener, final String title, final String url, final String summary, Bitmap bitmap /*String imageUrl*//*ArrayList<String> imageUrls*/) {

        this.mListener = listener;
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(summary)/*||TextUtils.isEmpty(imageUrl)*/ || bitmap == null || TextUtils.isEmpty(url)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBitmap(bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                ArrayList<String> imageUrls = new ArrayList<String>();
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
                //  params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
                imageUrls.add(ImageUrl);
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
                // QZone分享要在主线程做
                ThreadManager.getMainHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        if (null != mTencent) {
                            mTencent.shareToQzone((Activity) mContext, params, emIUiListener);
                        }
                    }
                });
            }
        });

      /*  try {
            saveBitmap(bitmap,mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> imageUrls = new ArrayList<String>();
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
      //  params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        imageUrls.add(ImageUrl);
       params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        // QZone分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQzone((Activity) mContext, params, emIUiListener);
                }
            }
        });*/
    }

    public void shareQzoneText(String summary, EmaSDKListener listener) {
        this.mListener = listener;
        if (TextUtils.isEmpty(summary)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        ArrayList<String> imageUrls = new ArrayList<String>();
        imageUrls.add(ImageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.publishToQzone((Activity) mContext, params, emIUiListener /*ThirdLoginUtils.getInstance(mActivity).emIUiListener*/);
                }
            }
        });

    }

    private void startPickLocaleImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (android.os.Build.VERSION.SDK_INT >= Build_VERSION_KITKAT) {
            intent.setAction(ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        ((Activity) mContext).startActivityForResult(
                Intent.createChooser(intent, "本地图片"), 0);
    }


    public IUiListener emIUiListener = new IUiListener() {
        public void onComplete(Object o) {
            Log.i(this.getClass().getName(), "QQShareUtils  onComplete---" + o.toString());
            if (o == null) {
                //  Toast.makeText(mContext,"分享失败",Toast.LENGTH_SHORT).show();
                mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "QQ分享失败");
            } else {
                JSONObject resultJson = (JSONObject) o;
                if (resultJson.optInt("ret") == 0) {
                    //  Toast.makeText(mContext,"分享成功",Toast.LENGTH_SHORT).show();
                    mListener.onCallBack(EmaCallBackConst.EMA_SHARE_OK, "分享成功");

                }
            }

        }

        @Override
        public void onError(UiError uiError) {
            //  Toast.makeText(mContext,uiError.errorMessage,Toast.LENGTH_SHORT).show();
            Log.i(this.getClass().getName(), "QQShareUtils  onError---");
            mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "分享失败");
        }

        @Override
        public void onCancel() {
            Log.i(this.getClass().getName(), "QQShareUtils  onCancel---");
            // Toast.makeText(mContext,"取消分享",Toast.LENGTH_SHORT).show();
            mListener.onCallBack(EmaCallBackConst.EMA_SHARE_CANCLE, "分享取消");
        }
    };

    public void saveBitmap(final Bitmap bitmap, final ShareQQImage shareQQImage) {
        new AsyncTask<Bitmap, Void, Void>() {
            @Override
            protected Void doInBackground(Bitmap... bitmaps) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
                String fileName = simpleDateFormat.format(new Date());
                File folder = new File("/mnt/sdcard/dcim/Camera/");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                File file = new File("/mnt/sdcard/dcim/Camera/" + fileName + ".jpg");
                //  Toast.makeText(context, "保存图片中", Toast.LENGTH_SHORT).show();
                FileOutputStream out;
                if (!file.exists()) {

                    try {
                        out = new FileOutputStream(file);
                        if (bitmaps[0].compress(Bitmap.CompressFormat.PNG, 70, out)) {
                   /* Toast.makeText(context, "成功存入相册",
                            Toast.LENGTH_SHORT).show();*/
                            ImageUrl = file.getAbsolutePath();
                            out.flush();
                            out.close();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                shareQQImage.shareImage();

            }
        }.execute(bitmap);

       /*  SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
        String fileName=simpleDateFormat.format(new Date());
        File folder = new File("/mnt/sdcard/dcim/Camera/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File("/mnt/sdcard/dcim/Camera/" + fileName + ".jpg");
      //  Toast.makeText(context, "保存图片中", Toast.LENGTH_SHORT).show();
        FileOutputStream out;
        if (!file.exists()) {

            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)) {
                   *//* Toast.makeText(context, "成功存入相册",
                            Toast.LENGTH_SHORT).show();*//*
                    ImageUrl=file.getAbsolutePath();
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    interface ShareQQImage {
        void shareImage();
    }

}

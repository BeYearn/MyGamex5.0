package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
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
    /* public static int Build_VERSION_KITKAT = 19;
     public static String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
     private static final String PATH_DOCUMENT = "document";*/
    private String imageUrl = "http://img7.doubanio.com/lpic/s3635685.jpg";
    public static final int SHARE_QQ_FRIEDNS_IMAGE = 0;
    public static final int SHARE_QQ_FRIEDNS_WEBPAGE = 1;
    public static final int SHARE_QQ_QZONE_TEXT = 2;
    public static final int SHARE_QQ_QZONE_IMAGE = 3;
    public static final int SHARE_QQ_QZONE_WEBPAGE = 4;

    public QQShareUtils(Context context) {
        this.mContext = context;
        mTencent = Tencent.createInstance(ConfigManager.getInstance(mContext).getQQAppId(), mContext);
    }

    public void shareQQFriendImage() {
        if (EmaSDK.getInstance().bitmap==null) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }
        saveBitmap(EmaSDK.getInstance().bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl);

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
    }

    public void shareQzoneImage() {
        if (EmaSDK.getInstance().bitmap==null) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }
        saveBitmap(EmaSDK.getInstance().bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, 3);
                ArrayList<String> imageUrls = new ArrayList<String>();
                imageUrls.add(imageUrl);
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
    }

    public void shareQQFriendsWebPage() {
        if (TextUtils.isEmpty(EmaSDK.getInstance().title) ||
                TextUtils.isEmpty(EmaSDK.getInstance().summary) || EmaSDK.getInstance().bitmap == null
                || TextUtils.isEmpty(EmaSDK.getInstance().url)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBitmap(EmaSDK.getInstance().bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                final Bundle params = new Bundle();
                params.putString(QQShare.SHARE_TO_QQ_TITLE, EmaSDK.getInstance().title);
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, EmaSDK.getInstance().url);
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, EmaSDK.getInstance().summary);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
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
    }

    public void shareQzoneWebPage() {

        // this.mListener = listener;
        if (TextUtils.isEmpty(EmaSDK.getInstance().title) || TextUtils.isEmpty(
                EmaSDK.getInstance().summary)||
                EmaSDK.getInstance().bitmap == null || TextUtils.isEmpty(EmaSDK.getInstance().url)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBitmap(EmaSDK.getInstance().bitmap, new ShareQQImage() {
            @Override
            public void shareImage() {
                ArrayList<String> imageUrls = new ArrayList<String>();
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, EmaSDK.getInstance().title);
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, EmaSDK.getInstance().url);
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, EmaSDK.getInstance().summary);
                //  params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
                imageUrls.add(imageUrl);
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


    }

    public void shareQzoneText() {
        if (TextUtils.isEmpty(EmaSDK.getInstance().summary)) {
            Toast.makeText(mContext, "请传入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, EmaSDK.getInstance().summary);
        ArrayList<String> imageUrls = new ArrayList<String>();
        imageUrls.add(imageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.publishToQzone((Activity) mContext, params, emIUiListener);
                }
            }
        });

    }

    public IUiListener emIUiListener = new IUiListener() {
        public void onComplete(Object o) {

            Log.i(this.getClass().getName(), "QQShareUtils  onComplete---" + o.toString());
            if (o == null) {
                EmaSDK.getInstance().mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "QQ分享失败");
            } else {
                JSONObject resultJson = (JSONObject) o;
                if (resultJson.optInt("ret") == 0) {
                    EmaSDK.getInstance().mListener.onCallBack(EmaCallBackConst.EMA_SHARE_OK, "分享成功");

                }
            }
            ((Activity) mContext).finish();
        }

        @Override
        public void onError(UiError uiError) {
            Log.i(this.getClass().getName(), "QQShareUtils  onError---");
            EmaSDK.getInstance().mListener.onCallBack(EmaCallBackConst.EMA_SHARE_FAIL, "分享失败");
            ((Activity) mContext).finish();
        }

        @Override
        public void onCancel() {
            Log.i(this.getClass().getName(), "QQShareUtils  onCancel---");
            EmaSDK.getInstance().mListener.onCallBack(EmaCallBackConst.EMA_SHARE_CANCLE, "分享取消");
            ((Activity) mContext).finish();
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
                File file = new File("/mnt/sdcard/dcim/Camera/" + fileName + ".png");
                //  Toast.makeText(context, "保存图片中", Toast.LENGTH_SHORT).show();
                FileOutputStream out;
                if (!file.exists()) {

                    try {
                        out = new FileOutputStream(file);
                        if (bitmaps[0].compress(Bitmap.CompressFormat.PNG, 70, out)) {

                            imageUrl = file.getAbsolutePath();
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

    }

    interface ShareQQImage {
        void shareImage();
    }

}

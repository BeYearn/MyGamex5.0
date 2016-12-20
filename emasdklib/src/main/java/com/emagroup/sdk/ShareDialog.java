package com.emagroup.sdk;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareDialog extends Dialog implements View.OnClickListener
{
    private static final String TAG = ShareDialog.class.getSimpleName();
    private static ResourceManager mResourceManager;

    private LinearLayout linear_weixin_friend;
    private LinearLayout linear_weixin_quan;
    private LinearLayout linear_sina_weibo;
    private LinearLayout linearLayout_qq;
    private LinearLayout linearLayout_qzone;
    public static  int SHARE_WEIBO=1;
    public static  int SHARE_WEIXIN_FRIENDS=2;
    public static  int SHARE_WEIXIN_QUAN=3;
    public static  int SHARE_QQ_FRIENDS=4;
    public static  int SHARE_QQ_QZONE=5;
   // private LinearLayout linear_bg;
    private TextView btn_close;
    private Context mContext;

    private OnBtnListener onBtnListener;

    public ShareDialog(Context context)
    {
        super(context);
    }

    protected ShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
    }

    public ShareDialog(Context context, int theme)
    {
        super(context, theme);
        mContext=context;
    }

    public static ShareDialog create(Context context) {

        mResourceManager=ResourceManager.getInstance(context);
        ShareDialog dialog = new ShareDialog(context,
                ResourceManager.getInstance(context).getIdentifier("TransparentDialog", "style"));
        dialog.setContentView(mResourceManager.getLayout("dialog_share"));
        dialog.setCancelable(true);


        return dialog;
    }

    public void showDialog()
    {
      //  linear_bg = (LinearLayout) findViewById(mResourceManager.getIdentifier("relative_bg", "id"));
        linear_weixin_friend = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_weixin_friend", "id"));
        linear_weixin_quan = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_weixin_quan", "id"));
        linear_sina_weibo = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_sina_weibo", "id"));
        linearLayout_qq = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_qq_share", "id"));
        linearLayout_qzone = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_qq_qzone", "id"));
        btn_close = (TextView) findViewById(mResourceManager.getIdentifier("btn_close", "id"));

        linearLayout_qq.setOnClickListener(this);
        linearLayout_qzone.setOnClickListener(this);
        linear_weixin_friend.setOnClickListener(this);
        linear_weixin_quan.setOnClickListener(this);
        linear_sina_weibo.setOnClickListener(this);
        btn_close.setOnClickListener(this);

        show();

        USharedPerUtil.setParam(mContext,"canWbShare",true);  //  避免微博分享如果有一次没回调，不置为true的话永远调不起微博分享
    }

    public void setOnBtnListener(OnBtnListener onBtnListener) {
        if(onBtnListener==null){
            Toast.makeText(mContext,"请传入正确参数",Toast.LENGTH_SHORT).show();
            return;
        }
       this.onBtnListener = onBtnListener;
    }

    @Override
    public void onClick(View v)
    {
        dismiss();

    //   int index = -1;
        if(v == linear_weixin_friend)
        {
          //  EmaSDK.getInstance().setPfType(SHARE_WEIXIN_FRIENDS);
          onBtnListener.onWechatFriendsClick();
            //index = 0;
        }
        else if(v == linear_weixin_quan)
        {
           // index = 1;
         //   EmaSDK.getInstance().setPfType(SHARE_WEIXIN_QUAN);
           onBtnListener.OnWechatQuanClick();
        }
        else if(v==linear_sina_weibo)
        {
         //   EmaSDK.getInstance().setPfType(SHARE_WEIBO);
           onBtnListener.onWeiBoClick();
           // index = 2;
        }else if(v==linearLayout_qq){
          //  EmaSDK.getInstance().setPfType(SHARE_QQ_FRIENDS);
          onBtnListener.OnQQClick();
        }else if(v==linearLayout_qzone){
         //   EmaSDK.getInstance().setPfType(SHARE_QQ_QZONE);
          onBtnListener.OnQZoneClick();
        }
      /*  if(onBtnListener != null)
        {
            onBtnListener.onClick(index);
        } */
    }

  /*@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();
        Rect mChangeImageBackgroundRect = new Rect();
        relative_bg.getDrawingRect(mChangeImageBackgroundRect);
        int[] location = new int[2];
        relative_bg.getLocationOnScreen(location);
        mChangeImageBackgroundRect.left = location[0];
        mChangeImageBackgroundRect.top = location[1];
        mChangeImageBackgroundRect.right = mChangeImageBackgroundRect.right + location[0];
        mChangeImageBackgroundRect.bottom = mChangeImageBackgroundRect.bottom + location[1];
        if(!mChangeImageBackgroundRect.contains(x, y))
        {
            dismiss();
        }
        return super.onTouchEvent(event);
    } */

  public interface OnBtnListener
    {
        void onWeiBoClick();
        void onWechatFriendsClick();
        void OnWechatQuanClick();
        void OnQQClick();
        void OnQZoneClick();
    }
}

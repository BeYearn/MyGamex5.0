package com.emagroup.sdk;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import static com.igexin.push.core.g.R;
import static com.igexin.push.core.g.c;

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
    private RelativeLayout relative_bg;
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
        relative_bg = (RelativeLayout)findViewById(mResourceManager.getIdentifier("relative_bg", "id"));
        linear_weixin_friend = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_weixin_friend", "id"));
        linear_weixin_quan = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_weixin_quan", "id"));
        linear_sina_weibo = (LinearLayout)findViewById(mResourceManager.getIdentifier("linear_sina_weibo", "id"));
        btn_close = (TextView) findViewById(mResourceManager.getIdentifier("btn_close", "id"));

        linear_weixin_friend.setOnClickListener(this);
        linear_weixin_quan.setOnClickListener(this);
        linear_sina_weibo.setOnClickListener(this);
        btn_close.setOnClickListener(this);

        show();
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
            onBtnListener.onWechatFriendsClick();
            //index = 0;
        }
        else if(v == linear_weixin_quan)
        {
           // index = 1;
            onBtnListener.OnWechatQuanClick();
        }
        else if(v==linear_sina_weibo)
        {
            onBtnListener.onWeiBoClick();
           // index = 2;
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
    }
}

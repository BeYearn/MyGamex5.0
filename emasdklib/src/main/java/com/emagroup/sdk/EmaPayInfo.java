package com.emagroup.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/8/23.
 */
public class EmaPayInfo implements Parcelable {

    private String productName;
    private String productNum;
    private String productId;

    //登录后才能拿到
    private String uid;

    //订单号，发起支付才能得到
    private String orderId;
    //钱包是否够
    private boolean coinEnough;
    //订单金额
    private int price;
    // 描述
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isCoinEnough() {
        return coinEnough;
    }

    public void setCoinEnough(boolean coinEnough) {
        this.coinEnough = coinEnough;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProductNum() {
        return productNum;
    }

    public void setProductNum(String productNum) {
        this.productNum = productNum;
    }

    public EmaPayInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeString(productNum);
        dest.writeString(productId);
        dest.writeString(orderId);
        dest.writeString(uid);
        dest.writeInt(price);
        dest.writeByte((byte) (coinEnough ? 1 : 0));     //if myBoolean == true, byte == 1
    }

    public EmaPayInfo(Parcel source) {
        //先读取mId，再读取mDate
        productName = source.readString();
        productNum = source.readString();
        productId = source.readString();
        orderId = source.readString();
        uid = source.readString();
        price = source.readInt();
        coinEnough = source.readByte() != 0;     //myBoolean == true if byte != 0
    }

    //实例化静态内部对象CREATOR实现接口Parcelable.Creator
    public static final Parcelable.Creator<EmaPayInfo> CREATOR = new Creator<EmaPayInfo>() {

        @Override
        public EmaPayInfo[] newArray(int size) {
            return new EmaPayInfo[size];
        }

        //将Parcel对象反序列化为ParcelableDate
        @Override
        public EmaPayInfo createFromParcel(Parcel source) {
            return new EmaPayInfo(source);
        }
    };

}

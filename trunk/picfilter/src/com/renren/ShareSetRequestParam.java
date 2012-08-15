package com.renren;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * 分享相关
 * @author Administrator
 *
 */
public class ShareSetRequestParam extends RequestParam implements Parcelable{
    /**
     * 请求的api
     */
    private static final String METHOD = "share.share";

    /**
     * 一条人人状态的字符长度上限
     */
    public static final int MAX_LENGTH = 140;

    /**
     * 状态的label，一般用来作为在Bundle等对象中使用时的key值
     */
    public static final String SHARE_LABEL = "share_set_request_param";

    /**
     * 状态文本信息
     */
    private String share;

    private String type;

    public ShareSetRequestParam(String share) {
        this.share = share;
    }
    
    public ShareSetRequestParam(String share,String type) {
        this.share = share;
        this.type = type;
    }

    public ShareSetRequestParam(Parcel in) {
        share = in.readString();
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (share != null) {
            dest.writeString(share);
        }
        if (type != null) {
            dest.writeString(type);
        }
    }

    public static final Parcelable.Creator<StatusSetRequestParam> CREATOR = new Parcelable.Creator<StatusSetRequestParam>() {
        public StatusSetRequestParam createFromParcel(Parcel in) {
            return new StatusSetRequestParam(in);
        }

        public StatusSetRequestParam[] newArray(int size) {
            return new StatusSetRequestParam[size];
        }
    };

    @Override
    public Bundle getParams() throws RenrenException {

        if (share == null || share.length() == 0) {
            String errorMsg = "Cannot send null status.";
            throw new RenrenException(RenrenError.ERROR_CODE_NULL_PARAMETER, errorMsg, errorMsg);
        }

        if (share.length() > MAX_LENGTH) {
            String errorMsg = "The length of the status should be smaller than " + StatusSetRequestParam.MAX_LENGTH
                    + " characters.";
            throw new RenrenException(RenrenError.ERROR_CODE_PARAMETER_EXTENDS_LIMIT, errorMsg, errorMsg);
        }

        Bundle params = new Bundle();
        params.putString("method", METHOD);
        params.putString("url", share);
        params.putString("type", type);
        return params;
    }
}

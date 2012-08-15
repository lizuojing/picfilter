package com.renren;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class ShareSetResponseBean extends ResponseBean implements Parcelable{

    private static final String RESULT = "id";

    public static final int DEFAULT_RESULT = 0;
    /**
     * status.get接口返回的result
     */
    private int result;

    public ShareSetResponseBean(String response) {
        super(response);
        try {
            JSONObject json = new JSONObject(response);
            result = json.getInt(RESULT);
        } catch (JSONException je) {
            result = DEFAULT_RESULT;
        }
    }

    public ShareSetResponseBean(Parcel in) {
        super("");
        result = in.readInt();
    }

    public ShareSetResponseBean(int result) {
        super("");
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
    }

    public static final Parcelable.Creator<StatusSetResponseBean> CREATOR = new Parcelable.Creator<StatusSetResponseBean>() {
        public StatusSetResponseBean createFromParcel(Parcel in) {
            return new StatusSetResponseBean(in);
        }

        public StatusSetResponseBean[] newArray(int size) {
            return new StatusSetResponseBean[size];
        }
    };

    @Override
    public String toString() {
        return "{result: " + result + "}";
    }

}

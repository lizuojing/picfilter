package com.renren;

import java.util.concurrent.Executor;

import org.json.JSONException;
import org.json.JSONObject;

import com.filter.api.ShareListener;
import com.filter.api.WeiboApi.AccountType;
import com.filter.log.LogUtils;

import android.os.Bundle;

public class ShareHelper {
    /**
     * 发送状态需要的权限
     */
    public static final String[] PUBLISH_STATUS_PERMISSIONS = { "share_share" };

    /**
     * 用来发送请求的{@link Renren}对象
     */
    private Renren renren;

    /**
     * StatusHelper构造函数
     * 
     * @param renren
     *          用来发送请求的{@link Renren}对象
     */
    public ShareHelper(Renren renren) {
        this.renren = renren;
    }

    /**
     * 发布一条状态到人人网
     * 
     * @param status
     *          要发布的状态对象
     * @return 
     *          若状态为空或者发送失败，会抛出异常，否则返回成功
     *          {@link FeedPublishResponseBean}对象
     * @throws RenrenException
     * @throws Throwable 
     */
    public ShareSetResponseBean publish(ShareSetRequestParam share) throws RenrenException, Throwable {
        if (!renren.isSessionKeyValid()) {
            String errorMsg = "Session key is not valid.";
            throw new RenrenException(RenrenError.ERROR_CODE_TOKEN_ERROR, errorMsg, errorMsg);
        }

        //参数不能为空
        if (share == null) {
            String errorMsg = "The parameter is null.";
            throw new RenrenException(RenrenError.ERROR_CODE_NULL_PARAMETER, errorMsg, errorMsg);
        }

        // 发布状态
        String response;
        try {
            Bundle params = share.getParams();
            response = renren.requestJSON(params);
        } catch (RenrenException rre) {
            LogUtils.e("ShareHelper", rre);
            throw rre;
        } catch (RuntimeException re) {
            LogUtils.e("ShareHelper", re);
            throw new Throwable(re);
        }

        RenrenError rrError = Util.parseRenrenError(response, Renren.RESPONSE_FORMAT_JSON);
        if (rrError != null) {
            throw new RenrenException(rrError);
        } else {
            try {
                JSONObject json = new JSONObject(response);
                LogUtils.i("WeiboApi", "share result is " + json.toString());
                if (json.optInt("id") !=0) {
                    return new ShareSetResponseBean(response);
                } else {
                    String errorMsg = "Cannot parse the response.";
                    throw new RenrenException(RenrenError.ERROR_CODE_UNABLE_PARSE_RESPONSE, errorMsg, errorMsg);
                }
            } catch (JSONException je) {
                LogUtils.e("ShareHelper", je);
                throw new RenrenException(RenrenError.ERROR_CODE_UNABLE_PARSE_RESPONSE, je.getMessage(), je
                        .getMessage());
            }
        }
    } // end of public Status publish(Activity activity, Status status,

    /**
     * 异步发送状态的方法
     * 
     * @param pool
     *            执行发送状态操作的线程池
     * @param status
     *            要发布的状态对象
     * @param listener
     *            用以监听发布状态结果的监听器对象
     * @param truncOption
     *            若超出了长度，是否自动截短至140个字
     */
    public void asyncPublish(Executor pool, final ShareSetRequestParam share,
            final ShareListener listener, final boolean truncOption) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ShareSetResponseBean stat = publish(share);
                    JSONObject json = new JSONObject(stat.toString());
                    int ret = json.getInt("result");
                    if(0!=ret) {
                        if (listener != null) {
                            listener.onReturnSucceedResult(AccountType.RENREN,stat.toString());
                        }
                    }else {
                        if (listener != null) {
                            listener.onReturnFailResult(AccountType.RENREN,ret,stat.toString());
                        }
                    }
                } catch (RenrenException rre) { // 参数、服务器等错误或异常
                    LogUtils.e("ShareHelper", rre);
                    if (listener != null) {
                        listener.onReturnFailResult(AccountType.RENREN,rre.getErrorCode(),rre.getMessage()+ rre.getOrgResponse());
                    }
                } catch (Throwable t) { // 运行时异常
                    LogUtils.e("ShareHelper", t);
                }
            }
        });
    }
}

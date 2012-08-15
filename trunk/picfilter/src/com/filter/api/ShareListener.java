package com.filter.api;

import com.filter.api.WeiboApi.AccountType;


/**
 * 当分享返回结果时调用此监听
 * 
 */
public interface ShareListener {

    /**
     * 请求成功时调用此方法
     * 
     * @param <T>
     * @param apiResult
     */
    <T> void onReturnSucceedResult(AccountType type,String result);

    /**
     * 请求失败或发生错误调用此方法
     * @param ret 
     * 
     * @param <T>
     * @param apiResult
     */
    <T> void onReturnFailResult(AccountType type ,int ret, String result);
    /**
     * 请求异常
     * 
     * @param <T>
     * @param apiResult
     */
    <T> void onReturnFailResult(AccountType type,Exception e);

}

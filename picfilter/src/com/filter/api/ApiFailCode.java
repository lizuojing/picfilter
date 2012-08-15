package com.filter.api;

public class ApiFailCode {
	public static final int UN_LOGIN = 0xFFFF1001;

	public static final int USER_JID_BANED = 21104;

	public static final int SYSTEM_BUSY = 10009;

	public static final int LINK_VOTED_BY_THE_USER = 30010;
	public static final int LINK_VOTED_MORE = 30013;
	
	public static final int LINK_NOT_EXISTED = 30001;
	public static final int LINK_URL_OVER_LENGTH = 30002;
	public static final int LINK_TITLE_OVER_LENGTH = 30003;
	public static final int LINK_PUBLISH_OVER_INTERVAL_TIME = 30005;
	public static final int LINK_HAD_BAN_FOR_THE_DOMAIN = 30006;
	public static final int LINK_HAD_DELETED = 30007;
	public static final int LINK_HAD_PUBLISHED_BY_USER= 30008;
	public static final int LINK_HAD_PUBLISHED_BY_OTHERS = 30009;
	

	public static final int COMMENT_NO_EXISTED = 40001;
	public static final int COMMENT_VOTED_BY_THE_USER = 40002;
	public static final int COMMNET_NOT_OVER_10_SECONDS = 40003;
	public static final int COMMENT_CONTENT_OVER_THE_LENGTH = 40004;

	public static final int REG_USER_ALREADY_EXIST = 409;
	public static final int REG_EMAIL_ALREADY_USED = 419;
}

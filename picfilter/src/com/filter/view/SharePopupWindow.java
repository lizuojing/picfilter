package com.filter.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.filter.R;
import com.filter.activity.WeiboActivity;
import com.filter.api.ShareListener;
import com.filter.api.WeiboApi;
import com.filter.api.WeiboApi.AccountType;
import com.filter.config.CacheConfig;
import com.filter.config.Config;
import com.filter.log.LogUtils;
import com.filter.utils.CompressPic;
import com.filter.utils.StringUtils;
import com.filter.utils.ToastManager;
import com.filter.utils.Utils;

public class SharePopupWindow extends PopupWindow implements OnClickListener, ShareListener {

	private static final String TAG = "SharePopupWindow";

	private LayoutInflater inflater;

	private Context context;
	private int backgroundRes = R.drawable.bg_share_popup_1;
	private int offX = 10;
	private int offY = 10;
	private int orientation;
	private String picUrl;

	private SPScrollView bgView;
	private ViewGroup rootView;
	private CheckBox checkSina;
	private CheckBox checkTencent;
	private CheckBox checkRenren;
	private Button btnSina;
	private Button btnTencent;
	private Button btnRenren;
	private SPEditText editText;
	private TextView tvInputLimit;
	private Button btnShare;

	public SharePopupWindow(Context context,String picUrl, int backgroundRes) {
		super(context);
		this.context = context;
		this.picUrl = picUrl;
		this.backgroundRes = backgroundRes;
		this.setFocusable(true);

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = (ViewGroup) inflater.inflate(R.layout.share_popup, null);
		setContentView(rootView);
		setWidth(LayoutParams.FILL_PARENT);
		setHeight(LayoutParams.FILL_PARENT);
		setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.translucence)));
		setAnimationStyle(R.style.main_menu_style);
		initLayout();

		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dismiss();
				return false;
			}
		});
	}

	private void refreshLocation() {
		rootView.setPadding(offX < 0 ? 0 : offX, rootView.getPaddingTop(), rootView.getPaddingRight(), offY < 0 ? 0
				: offY);
		setWindowOrientation();
	}

	public void show(View parent, int orientation, int offX, int offY) {
		this.offX = offX;
		this.offY = offY;
		this.orientation = orientation;
		refreshLocation();
		super.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}

	public void initLayout() {
		bgView = (SPScrollView) rootView.findViewById(R.id.background);
		bgView.setBackgroundResource(backgroundRes);

		checkSina = (CheckBox) rootView.findViewById(R.id.check_sina);
		checkTencent = (CheckBox) rootView.findViewById(R.id.check_tencent);
		checkRenren = (CheckBox) rootView.findViewById(R.id.check_renren);
		btnSina = (Button) rootView.findViewById(R.id.btn_sina);
		btnTencent = (Button) rootView.findViewById(R.id.btn_tencent);
		btnRenren = (Button) rootView.findViewById(R.id.btn_renren);


		editText = (SPEditText) rootView.findViewById(R.id.edit);
		tvInputLimit = (TextView) rootView.findViewById(R.id.tv_input_limit);
		btnShare = (Button) rootView.findViewById(R.id.btn_share);
		
		bgView.setSpEditText(editText);
		editText.setOnClickListener(this);
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							((ScrollView) bgView).smoothScrollBy(0, editText.getBottom());
						}
					}, 300);
				}
			}
		});

		btnSina.setOnClickListener(this);
		btnTencent.setOnClickListener(this);
		btnRenren.setOnClickListener(this);
		btnShare.setOnClickListener(this);
		refreshLayout();
	}

	public void refreshLayout() {
		initEditText();
		refreshWeiboButton();
	}

	public void refreshWeiboButton() {
		boolean hasSinaBind = WeiboApi.hasSinaBind(context);
		checkSina.setVisibility(hasSinaBind ? View.VISIBLE : View.INVISIBLE);
		btnSina.setVisibility(hasSinaBind ? View.INVISIBLE : View.VISIBLE);

		boolean hasTencentBind = WeiboApi.hasTencentBind(context);
		checkTencent.setVisibility(hasTencentBind ? View.VISIBLE : View.INVISIBLE);
		btnTencent.setVisibility(hasTencentBind ? View.INVISIBLE : View.VISIBLE);

		boolean hasRenrenBind = WeiboApi.hasRenrenBind(context);
		checkRenren.setVisibility(hasRenrenBind ? View.VISIBLE : View.INVISIBLE);
		btnRenren.setVisibility(hasRenrenBind ? View.INVISIBLE : View.VISIBLE);
	}

	private void initEditText() {
		refreshTips(editText.getText());
		editText.addTextChangedListener(editTextWatcher);
	}

	/**
	 * 如果offX 和 offY
	 * 
	 * @param newConfig
	 * @param offX
	 * @param offY
	 */
	public void onConfigurationChanged(Configuration newConfig, int offX, int offY) {
		if (offX > 0) {
			this.offX = offX;
		}
		if (offY > 0) {
			this.offY = offY;
		}
		orientation = newConfig.orientation;
		refreshLocation();
	}

	private void setWindowOrientation() {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setWindowOrientationToLandscape();
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			setWindowOrientationToPortrait();
		}
	}

	private void setWindowOrientationToPortrait() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.check_sina);
		params.addRule(RelativeLayout.ALIGN_LEFT, R.id.check_sina);
		params.setMargins(0, Utils.dipToPixels(context, 8), 0, 0);
		editText.setLines(4);
	}

	private void setWindowOrientationToLandscape() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF, R.id.check_renren);
		params.addRule(RelativeLayout.ALIGN_TOP, R.id.check_renren);
		params.setMargins(Utils.dipToPixels(context, 10), 0, 0, 0);
		editText.setLines(2);
	};

	TextWatcher editTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			refreshTips(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			refreshTips(s);
		}

		@Override
		public void afterTextChanged(Editable s) {
			byte[] bs = refreshTips(s);
			if (bs.length > Config.SHARE_POPUP_INPUT_LIMIT) {
				String str = StringUtils.getStringFromByteArray(bs, Config.SHARE_POPUP_INPUT_LIMIT, "GBK");
				if (s.length() > str.length()) {
					s.delete(str.length(), s.length());
				}
			}
		}
	};


	private byte[] refreshTips(CharSequence s) {
		byte[] bs = new byte[0];
		try {
			bs = s.toString().getBytes("GBK");
			int length = bs.length;
			tvInputLimit.setText(String.format(context.getString(R.string.share_popup_input_limit),
					(Config.SHARE_POPUP_INPUT_LIMIT - length) / 2));
		} catch (UnsupportedEncodingException e) {
			LogUtils.e(TAG, e);
		}
		return bs;
	}

	public int getBackgroundRes() {
		return backgroundRes;
	}

	public void setBackgroundRes(int backgroundRes) {
		this.backgroundRes = backgroundRes;
	}

	public int getOffX() {
		return offX;
	}

	public void setOffX(int offX) {
		this.offX = offX;
	}

	public int getOffY() {
		return offY;
	}

	public void setOffY(int offY) {
		this.offY = offY;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit:
			if (bgView instanceof ScrollView) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						((ScrollView) bgView).smoothScrollBy(0, editText.getBottom());
					}
				}, 300);
			}
			break;
		case R.id.btn_sina:
			checkSina.setChecked(true);
			goBindWeibo(AccountType.SINA);
			break;
		case R.id.btn_tencent:
			checkTencent.setChecked(true);
			goBindWeibo(AccountType.TENCENT);
			break;
		case R.id.btn_renren:
			checkRenren.setChecked(true);
			goBindWeibo(AccountType.RENREN);
			break;
		case R.id.btn_share:
			String content = editText.getText().toString();
			if (StringUtils.isNullOrEmpty(content)) {
				if (context instanceof Activity) {
					ToastManager.showToast((Activity) context, R.string.toast_share_content_null);
				}
				return;
			}
			WeiboApi api = new WeiboApi(context);
			ArrayList<WeiboApi.AccountType> list = new ArrayList<WeiboApi.AccountType>();
			if (checkSina.getVisibility() == View.VISIBLE && checkSina.isChecked()) {
				list.add(AccountType.SINA);
			}
			if (checkTencent.getVisibility() == View.VISIBLE && checkTencent.isChecked()) {
				list.add(AccountType.TENCENT);
			}
			if (checkRenren.getVisibility() == View.VISIBLE && checkRenren.isChecked()) {
				list.add(AccountType.RENREN);
			}
			if (list.size() <= 0) {
				ToastManager.showToast((Activity) context, R.string.toast_share_type_list_null);
				return;
			}
			api.shareContent(context, list, content, null, picUrl,this);
			ToastManager.showToast((Activity) context, R.string.toast_share_has_sent);
			dismiss();
			break;
		}
	}

	private void goBindWeibo(AccountType type) {
		Intent intent = new Intent(context, WeiboActivity.class);
		switch (type) {
		case SINA:
			intent.putExtra(WeiboActivity.INTENT_WEIBO_TYPE, WeiboActivity.TYPE_SINA);
			break;
		case TENCENT:
			intent.putExtra(WeiboActivity.INTENT_WEIBO_TYPE, WeiboActivity.TYPE_TENCENT);
			break;
		case RENREN:
			intent.putExtra(WeiboActivity.INTENT_WEIBO_TYPE, WeiboActivity.TYPE_RENREN);
			break;
		}
		context.startActivity(intent);
	}

	@Override
	public <T> void onReturnFailResult(AccountType type, Exception e) {
		LogUtils.e(TAG, e);

	}

	@Override
	public <T> void onReturnSucceedResult(AccountType type, String result) {
		LogUtils.i(TAG, "onReturnSucceedResult result is " + result);
	}

	@Override
	public <T> void onReturnFailResult(AccountType type, int ret, String result) {
		LogUtils.i(TAG, "onReturnFailResult type is " + type + " ret is " + ret + " result is " + result);
		switch (type) {
		case SINA:
			if (21315 == ret || 21316 == ret || 21317 == ret) {
				// accessToken 失效 需要重新验证
				WeiboApi.unBindAccount(AccountType.QZONE, context);
			}
			break;
		case TENCENT:
			if (1 == ret || 3 == ret || 4 == ret) {
				WeiboApi.unBindAccount(AccountType.TENCENT, context);
			}
			break;
		case RENREN:
			if (2001 == ret || 2002 == ret) {
				WeiboApi.unBindAccount(AccountType.RENREN, context);
			}
			break;
		case QZONE:
			if (9016 == ret || 9017 == ret || 9018 == ret || 9094 == ret || 100013 == ret || 100014 == ret
					|| 100015 == ret || 41003 == ret) {
				// accessToken 失效 需要重新验证
				WeiboApi.unBindAccount(AccountType.QZONE, context);
			}
			break;

		default:
			break;
		}
	}

}

class SPEditText extends EditText {

	private boolean isCanScroll;
	private static final String TAG="SPEditText";
	
	public boolean isCanScroll() {
		return isCanScroll;
	}

	public SPEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SPEditText(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(this.getHeight()/this.getLineHeight()>=this.getLineCount()){
			isCanScroll=false;
		}else{
			isCanScroll=true;
		}
		boolean b = false;
		try {
			b = super.onTouchEvent(event);
		} catch (Exception e) {
			LogUtils.e(TAG, e);
		}
		return b;
	}
}

class SPScrollView extends ScrollView {

	private SPEditText spEditText;
	
	public void setSpEditText(SPEditText spEditText) {
		this.spEditText = spEditText;
	}

	public SPScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SPScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SPScrollView(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(spEditText!=null&&spEditText.isCanScroll()){
			return false;
		}else{
			return super.onInterceptTouchEvent(ev);
		}
	}

}

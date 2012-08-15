package com.filter.utils;

import java.io.UnsupportedEncodingException;


public class StringUtils {


	public static boolean isNullOrEmpty(String str) {
		boolean flag = true;
		if (str != null && !"".equals(str.trim())) {
			flag = false;
		}
		return flag;
	}

	public static boolean isNotNullOrEmpty(String str) {
		return !isNullOrEmpty(str);
	}

	/**
	 * 将byte数组转换为字符串,
	 * 
	 * @param array
	 *            byte数组
	 * @param length
	 *            截取的长度,从0开始,因中文字符等原因,实际截取的长度可能小于length
	 * @param charsetName
	 *            生成的字符串的字符编码
	 * @return
	 */
	public static String getStringFromByteArray(byte[] array, int length, String charsetName) {
		String str = "";
		int sig = 1;
		if (length <= array.length) {
			for (int i = 0; i < length; i++) {
				sig = array[i] * sig >= 0 ? 1 : -1;
			}
			if (sig < 0) {
				length -= 1;
			}
			try {
				str = new String(array, 0, length, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str;
	}
}

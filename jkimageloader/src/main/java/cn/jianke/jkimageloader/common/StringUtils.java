package cn.jianke.jkimageloader.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @className: StringUtils
 * @classDescription: String util
 * @author: leibing
 * @createTime: 2016/08/30
 */
public class StringUtils {

	/**
	 * determine whether is null or an empty string
	 * @author leibing
	 * @createTime 2016/08/30
	 * @lastModify 2016/08/30
	 * @param str
	 * @return
	 */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim())) {
            return true;
        }
        return false;
    }

	/**
	 * determine if no is null or not an empty string
	 * @author leibing
	 * @createTime 2016/08/30
	 * @lastModify 2016/08/30
	 * @param str
	 * @return
	 */
    public static boolean isNotEmpty(String str){
		if (str == null || str.trim().equals(""))
			return false;
		return true;
    }

	/**
	 * according to the class name for an object instance
	 * @author leibing
	 * @createTime 2016/08/30
	 * @lastModify 2016/08/30
	 * @param className
	 * @return
	 */
	public static Object getObject(String className){
		Object object = null;
		if(StringUtils.isNotEmpty(className)){
			try {
				object = Class.forName(className).newInstance();
			}catch(ClassNotFoundException cnf) {
			}
			catch(InstantiationException ie) {
			}
			catch(IllegalAccessException ia) {
			}
		}
		return object;
	}

	/**
	 * to determine whether a string figures
	 * @author leibing
	 * @createTime 2016/11/17
	 * @lastModify 2016/11/17
	 * @param
	 * @return
	 */
	public static boolean strIsNum(String str){
		// determine whether is null or an empty string
		if (StringUtils.isEmpty(str))
			return false;
		// remove the blank space
		str = str.trim();
		// matching
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ) {
			return false;
		}
		return true;
	}

	/**
	 * two items in a list of data exchange
	 * @author leibing
	 * @createTime 2016/12/3
	 * @lastModify 2016/12/3
	 * @param mList
	 * @param swapNum1
	 * @param swapNum2
	 * @return
	 */
	public static void listSwapItem(ArrayList mList, int swapNum1, int swapNum2){
		// if no data,just return it
		if (mList == null || mList.size() == 0)
			return;
		// if the index cross-border,just return it
		if (swapNum1 >= mList.size() || swapNum2 >= mList.size())
			return;
		// swap data manipulation
		mList.add(swapNum1, mList.get(swapNum2));
		mList.add(swapNum2 + 1, mList.get(swapNum1 + 1));
		mList.remove(swapNum1 + 1);
		mList.remove(swapNum2 + 1);
	}

	/**
	 * bytes to hex string
	 * @author leibing
	 * @createTime 2017/3/2
	 * @lastModify 2017/3/2
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * get hash key for url
	 * @author leibing
	 * @createTime 2017/3/2
	 * @lastModify 2017/3/2
	 * @param url
	 * @return
	 */
	public static String hashKeyFormUrl(String url) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(url.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(url.hashCode());
		}
		return cacheKey;
	}
}

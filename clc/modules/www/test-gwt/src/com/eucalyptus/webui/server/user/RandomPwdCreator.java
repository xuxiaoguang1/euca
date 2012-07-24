package com.eucalyptus.webui.server.user;

import java.util.Random;

public class RandomPwdCreator {
	
	public static String genRandomNum(int pwdLen){
		//35是因为数组是从0开始的，26个字母+10个数字
		final int  maxNum = 36;
		int i;
		int count = 0;
		char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
				  		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
				  		'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		  
		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while(count < pwdLen){
			//生成随机数，取绝对值，防止生成负数，
			i = Math.abs(r.nextInt(maxNum));  //生成的数最大为36-1
			
			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count ++;
		    }
		}
		  
		return pwd.toString();
	}
}

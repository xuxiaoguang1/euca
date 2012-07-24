package com.eucalyptus.webui.shared.checker;

public class InvalidValueExceptionMsg {

    public static String[] CONTENT_IS_EMPTY = {"Content can not be empty", "内容不能为空"};
    public static String[] ACCOUNT_NAME_IS_EMPTY = {"Account name can not be empty", "账户名称不能为空"};
    public static String[] ACCOUNT_NAME_START_WITH_HYPHEN = {"Account name can not start with hyphen", "账户名称首字母不能是连字号"};
    public static String[] ACCOUNT_NAME_HAVE_TWO_HYPHENS = {"Account name can not have two consecutive hyphens", "账户名称不能包含两个以上的连字号"};
    public static String[] ACCOUNT_WITH_INVALID_CHARACTER = {"Containing invalid character for account name: ", "账户名称包含非法字符"};
    public static String[] USER_OR_GROUP_NAME_IS_EMPTY = {"User or group names can not be empty", "用户或组名称不能为空"};
    public static String[] USER_OR_GROUP_NAME_WITH_INVALID_CHARACTER = {"Containing invalid character for user or group names: ", "用户或组名称包含非法字符"};
    public static String[] PATH = {"Path must start with /", "路径必须以/起始"};
    public static String[] PATH_IS_INVALID = {"Invalid path character: ", "路径包含非法字符"};
    public static String[] PWD_IS_EMPTY = {"Password can not be empty", "密码不能为空"};
    public static String[] PWD_AT_LEAST_SIX_CHARACTERS = {"Password length must be at least 6 characters", "密码长度必须包含至少六个字母"};
    public static String[] EMAIL_IS_EMPTY = {"Email address can not be empty", "邮件地址不能为空"};
    public static String[] EMAIL_IS_INVALID = {"Does not look like a valid email address: missing user or host", "邮件地址不是一个有效地址"};
    public static String[] EMAIL_WITH_SPACE = {"Email address can not have spaces", "邮件地址不能包含空格"}; 
}



package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.Place;

public class LoginPlace extends Place {
  
  public static final String DEFAULT_PROMPT = "";
  public static final String LOGIN_FAILURE_PROMPT[] = {"Login failed! Please try again.", "登录失败! 请重新尝试."};
  public static final String LOADING_FAILURE_PROMPT[] = {"Loading failed! Please contact administrator and try login again.", "系统运行故障! 请联系系统管理员并重新尝试."};
  
  private String prompt;
  
  public LoginPlace( String prompt ) {
    this.prompt = prompt;
  }
  
  public String getPrompt( ) {
    return this.prompt;
  }
  
}

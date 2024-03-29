package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.checker.ValueChecker;

public interface InputField {
  
  public enum ValueType {
    TEXT,
    TEXTAREA,
    PASSWORD,
    NEWPASSWORD, 
    LISTBOX,
  }
  
  String getTitle( );
  
  ValueType getType( );
  
  ValueChecker getChecker( );
  
}

package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResultRow;

public interface SearchTableCellClickHandler {

  void onClick( int rowIndex, int colIndex, SearchResultRow row);
  
}

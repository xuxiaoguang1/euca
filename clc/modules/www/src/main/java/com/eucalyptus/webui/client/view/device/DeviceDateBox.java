package com.eucalyptus.webui.client.view.device;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;

public class DeviceDateBox extends DateBox {
    
    private static final String DATE_BOX_FORMAT_ERROR = "dateBoxFormatError";
    
    private DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy-MM-dd");
    
    private Handler handler = null;
    
    public interface Handler {
    	
    	public void onErrorHappens();
    	
    	public void onValueChanged();
    	
    }
    
    public DeviceDateBox() {
        super();
        setFormat(new Format() {

            @Override
            public String format(DateBox dateBox, Date date) {
                if (date == null) {
                    return "";
                }
                return formatter.format(date);
            }

            @Override
            public Date parse(DateBox dateBox, String text, boolean reportError) {
                try {
                    if (!isEmpty(text)) {
                        return formatter.parse(text);
                    }
                }
                catch (IllegalArgumentException e) {
                    if (reportError) {
                    	addStyleName(DATE_BOX_FORMAT_ERROR);
                    	if (handler != null) {
                    		handler.onErrorHappens();
                    	}
                    }
                }
                return null;
            }

            @Override
            public void reset(DateBox dateBox, boolean abandon) {
            	removeStyleName(DATE_BOX_FORMAT_ERROR);
            }
            
        });
        addValueChangeHandler(new ValueChangeHandler<Date>() {
        	
        	private Date last = null;

			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Date date = formatter.parse(formatter.format(event.getValue()));
				if (date != null) {
					if (date.equals(last)) {
						return;
					}
				}
				else {
					if (last == null) {
						return;
					}
				}
				last = date;
				if (handler != null) {
					handler.onValueChanged();
				}
			}
			
        });
    }
    
    public void setErrorHandler(Handler handler) {
    	this.handler = handler;
    }
    
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	public boolean hasError() {
		Date date = getValue();
		if (date == null) {
			return !isEmpty(getText());
		}
		else {
			return !getText().equals(formatter.format(date));
		}
	}
	
	public String getText() {
		String text = getTextBox().getText();
		if (text == null) {
			return "";
		}
		return text;
	}

}

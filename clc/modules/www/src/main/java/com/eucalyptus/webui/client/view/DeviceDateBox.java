package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.activity.device.DeviceDate;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.datepicker.client.DateBox;

public class DeviceDateBox extends DateBox {
    
    private static final String DATE_BOX_FORMAT_ERROR = "dateBoxFormatError";
    
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
                return DeviceDate.format(date);
            }

            @Override
            public Date parse(DateBox dateBox, String text, boolean reportError) {
                try {
                    if (!isEmpty(text)) {
                        return DeviceDate.parse(text);
                    }
                }
                catch (Exception e) {
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
				Date date = null;
				try {
					date = DeviceDate.parse(DeviceDate.format(event.getValue()));
				}
				catch (Exception e) {
				}
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
        getTextBox().addValueChangeHandler(new ValueChangeHandler<String> () {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String text = event.getValue();
				if (isEmpty(text)) {
					if (handler != null) {
						handler.onValueChanged();
					}
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
			return !getText().equals(DeviceDate.format(date));
		}
	}
	
	public String getText() {
		String text = getTextBox().getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	public static HTML getDateErrorHTML(DeviceDateBox dateBox) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "无效的日期格式: "));
	    sb.append("</font>");
	    sb.append("<font color='").append("red").append("'>");
	    sb.append("'").append(dateBox.getText()).append("'");
	    sb.append("</font>");
	    sb.append("</div>");
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "请输入有效格式")).append(": 'YYYY-MM-DD'");
	    sb.append("</font>");
	    sb.append("<div>");
	    sb.append("</div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "例如: '2012-07-01'"));
	    sb.append("</font>");
	    sb.append("</div>");
	    return new HTML(sb.toString());
	}
	
	public static HTML getDateErrorHTML(DeviceDateBox box0, DeviceDateBox box1) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "无效的日期: "));
	    sb.append("</font>");
	    sb.append("</div>");
	    sb.append("<div>");
	    sb.append("<font color='").append("darkred").append("'>");
	    sb.append("'").append(box0.getText()).append("' > '").append(box1.getText()).append("'");
	    sb.append("</font>");
	    sb.append("</div>");
	    return new HTML(sb.toString());
	}

}

package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplatePriceAddViewImpl extends DialogBox implements DeviceTemplatePriceAddView {

	private static DeviceTemplatePriceAddViewImplUiBinder uiBinder = GWT.create(DeviceTemplatePriceAddViewImplUiBinder.class);
	
	interface DeviceTemplatePriceAddViewImplUiBinder extends UiBinder<Widget, DeviceTemplatePriceAddViewImpl> {
	}
	
	@UiField ListBox templateName;
	@UiField TextArea templatePriceDesc;
	@UiField IntegerBox cpuSize;
	@UiField DoubleBox cpuPrice;
	@UiField LongBox memTotal;
	@UiField DoubleBox memPrice;
	@UiField LongBox diskTotal;
	@UiField DoubleBox diskPrice;
	@UiField IntegerBox bwSize;
	@UiField DoubleBox bwPrice;
	@UiField DoubleBox totalPrice;
	
	private Map<String, Integer> templateMap = new HashMap<String, Integer>();
	
	public DeviceTemplatePriceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		ValueChangeHandler<Double> handler = new ValueChangeHandler<Double>() {

			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				updateTotalPrice();
			}
			
		};
		cpuPrice.addValueChangeHandler(handler);
		memPrice.addValueChangeHandler(handler);
		diskPrice.addValueChangeHandler(handler);
		bwPrice.addValueChangeHandler(handler);
		templateName.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
			    int template_id = getTemplateID();
			    if (template_id != -1) {
			        presenter.lookupTemplate(template_id);
			    }
			}
			
		});
		center();
		hide();
	}
	
	private DeviceTemplatePriceAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int template_id;
	private int ncpus;
	private double mem_total;
	private double disk_total;
	private double bw_size;
	
	private int getID(Map<String, Integer> map, String name) {
        if (name == null || name.isEmpty()) {
            return -1;
        }
        Integer id = map.get(name);
        if (id == null) {
            return -1;
        }
        return id;
    }
    
    private int getTemplateID() {
        return getID(templateMap, getSelectedText(templateName));
    }
    
    private String getSelectedText(ListBox listbox) {
        int index = listbox.getSelectedIndex();
        if (index == -1) {
            return "";
        }
        return listbox.getItemText(index);
    }
	
	private void updateTotalPrice() {
	    try {
	        double sum = 0;
	        double value;
	        if ((value = cpuPrice.getValue()) < 0 || ncpus < 0) {
	            totalPrice.setText("INVALID VALUE");
	            return;
	        }
	        sum += value * ncpus;
	        if ((value = memPrice.getValue()) < 0 || mem_total < 0) {
	            totalPrice.setText("INVALID VALUE");
                return;
	        }
	        sum += value * mem_total;
	        if ((value = diskPrice.getValue()) < 0 || disk_total < 0) {
	            totalPrice.setText("INVALID VALUE");
                return;
	        }
	        sum += value * disk_total;
	        if ((value = bwPrice.getValue()) < 0 || bw_size < 0) {
	            totalPrice.setText("INVALID VALUE");
                return;
	        }
	        sum += value * bw_size;
	        totalPrice.setValue(sum);
	    }
	    catch (Exception e) {
	        totalPrice.setText("INVALID VALUE");
	    }
	}
	
	private void resetTemplateDetail(boolean all) {
		memTotal.setText("");
		diskTotal.setText("");
		bwSize.setText("");
		if (all) {
			templatePriceDesc.setText("");
			cpuPrice.setText("");
			memPrice.setText("");
			diskPrice.setText("");
			bwPrice.setText("");
			totalPrice.setText("");
		}
	}
	
	@Override
	public void popup() {
		templateName.clear();
		resetTemplateDetail(true);
		presenter.lookupTemplates();
		show();
	}
	
    @Override
    public void setTemplates(Map<String, Integer> template_map) {
        templateName.clear();
        templateMap.clear();
        if (template_map != null && !template_map.isEmpty()) {
            List<String> list = new ArrayList<String>(template_map.keySet());
            Collections.sort(list);
            for (String template_name : list) {
                templateName.addItem(template_name);
            }
            templateName.setSelectedIndex(0);
            templateMap = template_map;
            int template_id = getTemplateID();
            if (template_id != -1) {
                presenter.lookupTemplate(template_id);
            }
        }
    }

    @Override
    public void setTemplate(int template_id, String template_name, int ncpus, double tp_cpu, long mem_total, double tp_mem, long disk_total, double tp_disk, int bw_size, double tp_bw) {
        if (getTemplateID() == template_id) {
			this.template_id = template_id;
			this.ncpus = ncpus;
			this.mem_total = mem_total;
			this.disk_total = disk_total;
			this.bw_size = bw_size;
	        cpuSize.setValue(ncpus);
			memTotal.setValue(mem_total);
			diskTotal.setValue(disk_total);
			bwSize.setValue(bw_size);
			cpuPrice.setValue(tp_cpu);
            memPrice.setValue(tp_mem);
            diskPrice.setValue(tp_disk);
            bwPrice.setValue(tp_bw);
            totalPrice.setValue(0.0);
			updateTotalPrice();
		}
	}
	
    private String getTemplatePriceDesc() {
    	String text = templatePriceDesc.getText();
    	if (text == null) {
    		return "";
    	}
    	return text;
    }
    
    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
    	if (presenter.onOK(template_id, getTemplatePriceDesc(), cpuPrice.getValue(), memPrice.getValue(), diskPrice.getValue(), bwPrice.getValue())) {
    		hide();
    	}
    }
    
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}

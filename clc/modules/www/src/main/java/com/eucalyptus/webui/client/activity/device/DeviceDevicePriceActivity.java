package com.eucalyptus.webui.client.activity.device;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.DeviceDevicePriceModifyView;
import com.eucalyptus.webui.client.view.DeviceDevicePriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceDevicePriceView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.DevicePriceInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceDevicePriceActivity extends DeviceActivity implements DeviceDevicePriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("DevicePrice", "设备定价");
	
	private DeviceDevicePriceModifyViewImpl modifyPanel = new DeviceDevicePriceModifyViewImpl();
	
	public DeviceDevicePriceActivity(SearchPlace place, ClientFactory clientFactory) {
	    super(place, clientFactory);
	}
	
	private DeviceDevicePriceView getView() {
		DeviceDevicePriceView view = (DeviceDevicePriceView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceDevicePriceView();
			view.setPresenter(this);
			container.setWidget(view);
			this.view = view;
		}
		return view;
	}
	
    @Override
    protected void doSearch(String query, SearchRange range) {
        reload();
    }
    

    @Override
    protected void showView(SearchResult result) {
        /* do nothing */
    }
    
    private void reload() {
        getBackendService().lookupDeviceCPUPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                getView().setCPUPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
        getBackendService().lookupDeviceMemoryPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                getView().setMemoryPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
        getBackendService().lookupDeviceDiskPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                getView().setDiskPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
        getBackendService().lookupDeviceBWPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                getView().setBWPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
    }

    @Override
    protected String getTitle() {
        return title.toString();
    }
    
    @Override
    public void onModifyCPUPrice(String op_desc, double op_price) {
        try {
            if (Window.confirm(new ClientMessage("Create a new CPU Price.", "确认创建新CPU定价.").toString())) {
                modifyPanel.popup(new ClientMessage("CPU Price", "CPU定价").toString(), new ClientMessage("Price: (Y/Day)", "单价: (元/天)").toString(), 
                        op_desc, op_price, new DeviceDevicePriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String op_desc, double op_price) {
                        if (op_price < 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(new ClientMessage("Invalid CPU Price: ", "CPU价格非法")).append(" = ").append(op_price).append(".").append("\n");
                            sb.append(new ClientMessage("Please try again.", "请重试."));
                            Window.alert(sb.toString());
                            return false;
                        }
                        getBackendService().modifyDeviceCPUPrice(getSession(), op_desc, op_price, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully create CPU Price.", "CPU价格添加成功."));
                                reload();
                            }
                            
                        });
                        return true;
                    }
                    
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }

	@Override
	public void onModifyMemoryPrice(String op_desc, double op_price) {
	    try {
	        if (Window.confirm(new ClientMessage("Create a new Memory Price.", "确认创建新内存定价.").toString())) {
	            modifyPanel.popup(new ClientMessage("Memory Price", "内存定价").toString(), new ClientMessage("Price: (Y/MB/Day)", "单价: (元/MB/天)").toString(), 
	                    op_desc, op_price, new DeviceDevicePriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String op_desc, double op_price) {
                        if (op_price < 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(new ClientMessage("Invalid Memory Price: ", "内存价格非法")).append(" = ").append(op_price).append(".").append("\n");
                            sb.append(new ClientMessage("Please try again.", "请重试."));
                            Window.alert(sb.toString());
                            return false;
                        }
                        getBackendService().modifyDeviceMemoryPrice(getSession(), op_desc, op_price, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
							}

							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully create Memory Price.", "内存价格添加成功."));
                                reload();
							}
							
                    	});
                    	return true;
                    }
                    
                });
	        }
		}
	    catch (Exception e) {
	        e.printStackTrace();
	        onFrontendServiceFailure(e);
	    }
	}
	
	@Override
	public void onModifyDiskPrice(String op_desc, double op_price) {
        try {
            if (Window.confirm(new ClientMessage("Create a new Disk Price.", "确认创建新硬盘定价.").toString())) {
                modifyPanel.popup(new ClientMessage("Disk Price", "硬盘定价").toString(), new ClientMessage("Price: (Y/MB/Day)", "单价: (元/MB/天)").toString(), 
                        op_desc, op_price, new DeviceDevicePriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String op_desc, double op_price) {
                        if (op_price < 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(new ClientMessage("Invalid Disk Price: ", "硬盘价格非法")).append(" = ").append(op_price).append(".").append("\n");
                            sb.append(new ClientMessage("Please try again.", "请重试."));
                            Window.alert(sb.toString());
                            return false;
                        }
                        getBackendService().modifyDeviceDiskPrice(getSession(), op_desc, op_price, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully create Disk Price.", "硬盘价格添加成功."));
                                reload();
                            }
                            
                        });
                        return true;
                    }
                    
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }
	
	@Override
    public void onModifyBWPrice(String op_desc, double op_price) {
        try {
            if (Window.confirm(new ClientMessage("Create a new Bandwidth Price.", "确认创建新带宽定价.").toString())) {
                modifyPanel.popup(new ClientMessage("Bandwidth Price", "带宽定价").toString(), new ClientMessage("Price: (Y/KB/Day)", "单价: (元/KB/天)").toString(), 
                        op_desc, op_price, new DeviceDevicePriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String op_desc, double op_price) {
                        if (op_price < 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(new ClientMessage("Invalid Bandwidth Price: ", "带宽价格非法")).append(" = ").append(op_price).append(".").append("\n");
                            sb.append(new ClientMessage("Please try again.", "请重试."));
                            Window.alert(sb.toString());
                            return false;
                        }
                        getBackendService().modifyDeviceBWPrice(getSession(), op_desc, op_price, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully create Bandwidth Price.", "带宽价格添加成功."));
                                reload();
                            }
                            
                        });
                        return true;
                    }
                    
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }
}

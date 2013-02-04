package com.eucalyptus.webui.client.activity.device;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.DeviceOthersPriceModifyView;
import com.eucalyptus.webui.client.view.DeviceOthersPriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceOthersPriceView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.OthersPriceInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceOthersPriceActivity extends DeviceActivity implements DeviceOthersPriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("OthersPrice", "其他定价");
	
	private DeviceOthersPriceModifyViewImpl modifyPanel = new DeviceOthersPriceModifyViewImpl();
	
	public DeviceOthersPriceActivity(SearchPlace place, ClientFactory clientFactory) {
	    super(place, clientFactory);
	}
	
	private DeviceOthersPriceView getView() {
		DeviceOthersPriceView view = (DeviceOthersPriceView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceOthersPriceView();
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
        getBackendService().lookupDeviceMemoryPrice(getSession(), new AsyncCallback<OthersPriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(OthersPriceInfo info) {
                getView().setMemoryPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
        getBackendService().lookupDeviceDiskPrice(getSession(), new AsyncCallback<OthersPriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(OthersPriceInfo info) {
                getView().setDiskPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
        getBackendService().lookupDeviceBWPrice(getSession(), new AsyncCallback<OthersPriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
                displayData(null);
            }

            @Override
            public void onSuccess(OthersPriceInfo info) {
                getView().setBWPrice(info.op_desc, info.op_price, info.op_modifiedtime);
            }
            
        });
    }

    @Override
    protected String getTitle() {
        return title.toString();
    }

	@Override
	public void onModifyMemoryPrice(String op_desc, double op_price) {
	    try {
	        if (Window.confirm(new ClientMessage("Create a new Memory Price.", "确认创建新内存定价.").toString())) {
	            modifyPanel.popup(new ClientMessage("Memory Price", "内存定价").toString(), new ClientMessage("Price: (Y/MB/Day)", "单价: (元/MB/天)").toString(), 
	                    op_desc, op_price, new DeviceOthersPriceModifyView.Presenter() {
                    
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
                        op_desc, op_price, new DeviceOthersPriceModifyView.Presenter() {
                    
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
                        op_desc, op_price, new DeviceOthersPriceModifyView.Presenter() {
                    
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

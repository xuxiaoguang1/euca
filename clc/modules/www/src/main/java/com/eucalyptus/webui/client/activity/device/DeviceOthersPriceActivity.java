package com.eucalyptus.webui.client.activity.device;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.ActivityUtil;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.DeviceOthersPriceModifyView;
import com.eucalyptus.webui.client.view.DeviceOthersPriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceOthersPriceView;
import com.eucalyptus.webui.client.view.LoadingAnimationView;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public class DeviceOthersPriceActivity extends AbstractActivity implements DeviceOthersPriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("OthersPrice", "CPU定价");
	
	private ClientFactory clientFactory;
	private AcceptsOneWidget container;
	private IsWidget view;
	
	private DeviceOthersPriceModifyViewImpl modifyPanel;
	
	public DeviceOthersPriceActivity(SearchPlace place, ClientFactory clientFactory) {
	    this.clientFactory = clientFactory;
	}
	
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        this.container = panel;
        this.clientFactory.getShellView().hideDetail();
        this.clientFactory.getShellView().getContentView().setContentTitle(getTitle());
        LoadingAnimationView view = this.clientFactory.getLoadingAnimationView();
        container.setWidget(view);
        onLoad();
        modifyPanel = new DeviceOthersPriceModifyViewImpl();
        ActivityUtil.updateDirectorySelection(clientFactory);
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
	
	private EucalyptusServiceAsync getBackendService() {
		return clientFactory.getBackendService();
	}

	private FooterView getFooterView() {
		return clientFactory.getShellView().getFooterView();
	}

	private LogView getLogView() {
		return clientFactory.getShellView().getLogView();
	}

	private Session getSession() {
		return clientFactory.getLocalSession().getSession();
	}
	
	private void showStatus(ClientMessage msg) {
		getFooterView().showStatus(StatusType.NONE, msg.toString(), FooterView.CLEAR_DELAY_SECOND * 3);
		getLogView().log(LogType.INFO, msg.toString());
	}
	
	private void onFrontendServiceFailure(Throwable caught) {
		Window.alert(new ClientMessage("", "前端服务运行错误").toString());
		getLogView().log(LogType.ERROR, caught.toString());
	}
	
	private void onBackendServiceFailure(Throwable caught) {
		if (caught instanceof EucalyptusServiceException) {
			EucalyptusServiceException exception = (EucalyptusServiceException)caught;
			ClientMessage msg = exception.getFrontendMessage();
			if (msg == null) {
				msg = new ClientMessage("Backend Service Failure", "后代服务运行错误");
			}
			Window.alert(msg.toString());
			getLogView().log(LogType.ERROR, msg.toString() + " : " + caught.toString());
		}
		else {
			getLogView().log(LogType.ERROR, caught.toString());
		}
	}
	
	private String getTitle() {
		return title.toString();
	}
	
	private static final int OTHERS_PRICE = 0;
	private static final int OTHERS_PRICE_DESC = 1;
	private static final int OTHERS_PRICE_MODIFIEDTIME = 2;
	
	private boolean isEmpty(String s) {
	    return s == null || s.length() == 0;
	}
	
	private void onLoad() {
	    getBackendService().lookupDeviceMemoryPrice(getSession(), new AsyncCallback<SearchResultRow>() {

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof EucalyptusServiceException) {
                    onBackendServiceFailure((EucalyptusServiceException)caught);
                }
            }

            @Override
            public void onSuccess(SearchResultRow result) {
                if (result != null) {
                    try {
                        double price = Double.parseDouble(result.getField(OTHERS_PRICE));
                        String price_desc = result.getField(OTHERS_PRICE_DESC);
                        String price_modifiedtime = result.getField(OTHERS_PRICE_MODIFIEDTIME);
                        getView().setMemoryPrice(price, price_desc, price_modifiedtime);
                    }
                    catch (Exception e) {
                        onFrontendServiceFailure(e);
                    }
                }
            }
            
	    });
        getBackendService().lookupDeviceDiskPrice(getSession(), new AsyncCallback<SearchResultRow>() {
            
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof EucalyptusServiceException) {
                    onBackendServiceFailure((EucalyptusServiceException)caught);
                }
            }
            
            @Override
            public void onSuccess(SearchResultRow result) {
                if (result != null) {
                    try {
                        double price = Double.parseDouble(result.getField(OTHERS_PRICE));
                        String price_desc = result.getField(OTHERS_PRICE_DESC);
                        String price_modifiedtime = result.getField(OTHERS_PRICE_MODIFIEDTIME);
                        getView().setDiskPrice(price, price_desc, price_modifiedtime);
                    }
                    catch (Exception e) {
                        onFrontendServiceFailure(e);
                    }
                }
            }
            
        });
        getBackendService().lookupDeviceBandwidthPrice(getSession(), new AsyncCallback<SearchResultRow>() {

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof EucalyptusServiceException) {
                    onBackendServiceFailure((EucalyptusServiceException)caught);
                }
            }

            @Override
            public void onSuccess(SearchResultRow result) {
                if (result != null) {
                    try {
                        double price = Double.parseDouble(result.getField(OTHERS_PRICE));
                        String price_desc = result.getField(OTHERS_PRICE_DESC);
                        String price_modifiedtime = result.getField(OTHERS_PRICE_MODIFIEDTIME);
                        getView().setBandwidthPrice(price, price_desc, price_modifiedtime);
                    }
                    catch (Exception e) {
                        onFrontendServiceFailure(e);
                    }
                }
            }
            
        });
	}

	@Override
	public void onModifyMemoryPrice(String price, String price_desc) {
		if (Window.confirm(new ClientMessage("", "确认修改所选择的区域").toString())) {
			modifyPanel.popup("内存定价", "单价: (元/MB/天)", price, price_desc ,
                new DeviceOthersPriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String price, String price_desc) {
                    	double others_price = 0;
                    	try {
                    	    if (!isEmpty(price)) {
                    	        others_price = Double.parseDouble(price);
                    	    }
                    	}
						catch (Exception e) {
							StringBuilder sb = new StringBuilder();
							sb.append(new ClientMessage("", "价格非法")).append(" = '").append(price).append("' ");
							sb.append(new ClientMessage("", "请重新填写价格"));
							Window.alert(sb.toString());
							return false;
						}
                    	getBackendService().modifyDeviceMemoryPrice(getSession(), price_desc, others_price, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
							}

							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "修改内存定价成功"));
								onLoad();
							}
							
                    	});
                    	return true;
                    }
                    
                });
		}
	}

	@Override
	public void onModifyDiskPrice(String price, String price_desc) {
		if (Window.confirm(new ClientMessage("", "确认修改所选择的区域").toString())) {
			modifyPanel.popup("硬盘定价", "单价: (元/MB/天)", price, price_desc ,
	                new DeviceOthersPriceModifyView.Presenter() {
	                    
	                    @Override
	                    public boolean onOK(String price, String price_desc) {
	                    	double others_price = 0;
	                    	try {
	                    	    if (!isEmpty(price)) {
	                    	        others_price = Double.parseDouble(price);
	                    	    }
	                    	}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "价格非法")).append(" = '").append(price).append("' ");
								sb.append(new ClientMessage("", "请重新填写价格"));
								Window.alert(sb.toString());
								return false;
							}
	                    	getBackendService().modifyDeviceDiskPrice(getSession(), price_desc, others_price, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "修改硬盘定价成功"));
									onLoad();
								}
								
	                    	});
	                    	return true;
	                    }
	                    
	                });
		}
	}

	@Override
	public void onModifyBandwidthPrice(String price, String price_desc) {
		if (Window.confirm(new ClientMessage("", "确认修改所选择的区域").toString())) {
			modifyPanel.popup("带宽定价", "单价: (元/KB/天)", price, price_desc ,
                new DeviceOthersPriceModifyView.Presenter() {
                    
                    @Override
                    public boolean onOK(String price, String price_desc) {
                    	double others_price;
                    	try {
                    		others_price = Double.parseDouble(price);
                    	}
						catch (Exception e) {
							StringBuilder sb = new StringBuilder();
							sb.append(new ClientMessage("", "价格非法")).append(" = '").append(price).append("' ");
							sb.append(new ClientMessage("", "请重新填写价格"));
							Window.alert(sb.toString());
							return false;
						}
                    	getBackendService().modifyDeviceBandwidthPrice(getSession(), price_desc, others_price, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
							}

							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "修改带宽定价成功"));
								onLoad();
							}
							
                    	});
                    	return true;
                    }
                    
                });
		}
	}
	
}

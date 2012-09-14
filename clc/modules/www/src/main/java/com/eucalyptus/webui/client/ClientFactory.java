package com.eucalyptus.webui.client;

import com.eucalyptus.webui.client.service.AwsServiceAsync;
import com.eucalyptus.webui.client.service.CmdServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.session.LocalSession;
import com.eucalyptus.webui.client.session.SessionData;
import com.eucalyptus.webui.client.view.AccountAddView;
import com.eucalyptus.webui.client.view.AccountView;
import com.eucalyptus.webui.client.view.ActionResultView;
import com.eucalyptus.webui.client.view.AreaView;
import com.eucalyptus.webui.client.view.CPUStatView;
import com.eucalyptus.webui.client.view.CertView;
import com.eucalyptus.webui.client.view.CloudRegistrationView;
import com.eucalyptus.webui.client.view.ClusterCtrlView;
import com.eucalyptus.webui.client.view.ConfigView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.DeviceAreaView;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceView;
import com.eucalyptus.webui.client.view.DeviceCPUView;
import com.eucalyptus.webui.client.view.DeviceCabinetView;
import com.eucalyptus.webui.client.view.DeviceDiskView;
import com.eucalyptus.webui.client.view.DeviceIPView;
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.client.view.DeviceOthersPriceView;
import com.eucalyptus.webui.client.view.DeviceRoomView;
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceView;
import com.eucalyptus.webui.client.view.DeviceTemplateView;
import com.eucalyptus.webui.client.view.DeviceVMView;
import com.eucalyptus.webui.client.view.DiskStatView;
import com.eucalyptus.webui.client.view.ErrorSinkView;
import com.eucalyptus.webui.client.view.GroupAddView;
import com.eucalyptus.webui.client.view.GroupAddingUserListView;
import com.eucalyptus.webui.client.view.GroupDetailView;
import com.eucalyptus.webui.client.view.GroupListView;
import com.eucalyptus.webui.client.view.GroupView;
import com.eucalyptus.webui.client.view.HistoryView;
import com.eucalyptus.webui.client.view.ImageView;
import com.eucalyptus.webui.client.view.IndividualView;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.InstanceView;
import com.eucalyptus.webui.client.view.IpPermissionView;
import com.eucalyptus.webui.client.view.ItemView;
import com.eucalyptus.webui.client.view.KeyView;
import com.eucalyptus.webui.client.view.KeypairView;
import com.eucalyptus.webui.client.view.LoadingAnimationView;
import com.eucalyptus.webui.client.view.LoadingProgressView;
import com.eucalyptus.webui.client.view.LoginView;
import com.eucalyptus.webui.client.view.MemoryStatView;
import com.eucalyptus.webui.client.view.NodeCtrlView;
import com.eucalyptus.webui.client.view.PolicyView;
import com.eucalyptus.webui.client.view.ReportView;
import com.eucalyptus.webui.client.view.RunInstanceView;
import com.eucalyptus.webui.client.view.SecurityGroupView;
import com.eucalyptus.webui.client.view.ShellView;
import com.eucalyptus.webui.client.view.StartView;
import com.eucalyptus.webui.client.view.StorageCtrlView;
import com.eucalyptus.webui.client.view.UploadImageView;
import com.eucalyptus.webui.client.view.UserAddView;
import com.eucalyptus.webui.client.view.UserAppAddView;
import com.eucalyptus.webui.client.view.UserAppView;
import com.eucalyptus.webui.client.view.UserView;
import com.eucalyptus.webui.client.view.VmTypeView;
import com.eucalyptus.webui.client.view.WalrusCtrlView;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;

public interface ClientFactory {

	/**
	 * @return the default place.
	 */
	Place getDefaultPlace();

	/**
	 * @return the place for the error page.
	 */
	Place getErrorPlace();

	/**
	 * @return the event bus for the main activities.
	 */
	EventBus getMainEventBus();

	/**
	 * @return the place controller for the main activities.
	 */
	PlaceController getMainPlaceController();

	/**
	 * @return the activity manager for the main activities.
	 */
	ActivityManager getMainActivityManager();

	/**
	 * @return the place history handler for the main activities.
	 */
	PlaceHistoryHandler getMainPlaceHistoryHandler();

	/**
	 * @return the Historian for the main activities.
	 */
	Historian getMainHistorian();

	/**
	 * @return the event bus for the lifecycle activities.
	 */
	EventBus getLifecycleEventBus();

	/**
	 * @return the place controller for the lifecycle activities.
	 */
	PlaceController getLifecyclePlaceController();

	/**
	 * @return the impl. of local session record, essentially the session ID.
	 */
	LocalSession getLocalSession();

	/**
	 * @return the local session data.
	 */
	SessionData getSessionData();

	/**
	 * @return the impl. of Euare service.
	 */
	EucalyptusServiceAsync getBackendService( );
	AwsServiceAsync getBackendAwsService( );
	CmdServiceAsync getBackendCmdService( );

	LoginView getLoginView();

	LoadingProgressView getLoadingProgressView();

	ShellView getShellView();

	StartView getStartView();

	ConfigView getConfigView();

	LoadingAnimationView getLoadingAnimationView();

	ErrorSinkView getErrorSinkView();

	AccountView getAccountView();

	VmTypeView getVmTypeView();

	ReportView getReportView();

	GroupView getGroupView();

	UserView getUserView();
	
	UserAppView getUserAppView();

	PolicyView getPolicyView();

	KeyView getKeyView();

	CertView getCertView();

	ImageView getImageView();

	ConfirmationView getConfirmationView();

	InputView getInputView();

	ActionResultView getActionResultView();

	ItemView createItemView();
	
	UploadImageView createUploadImageView();
	RunInstanceView createRunInstanceView();
	AreaView createAreaView();

	CloudRegistrationView getCloudRegistrationView();

	UserAddView getUserAddView();

	GroupAddView getGroupAddView();
	
	AccountAddView getAccountAddView();

	GroupListView getGroupListView();

	GroupDetailView getGroupDetailView();

	GroupAddingUserListView getGroupAddingUserListView();

	IndividualView getIndividualView();

	DeviceServerView getDeviceServerView();

	DeviceCPUView getDeviceCPUView();

	DeviceMemoryView getDeviceMemoryView();

	DeviceDiskView getDeviceDiskView();

	DeviceIPView getDeviceIPView();
	
	DeviceVMView getDeviceVMView();
	
	DeviceBWView getDeviceBWView();
	
	DeviceTemplateView getDeviceTemplateView();
	
	UserAppAddView getUserAppAddView();
	
	DeviceAreaView getDeviceAreaView();
	
	DeviceRoomView getDeviceRoomView();
	
	DeviceCabinetView getDeviceCabinetView();
	
	DeviceCPUPriceView getDeviceCPUPriceView();
	
	DeviceOthersPriceView getDeviceOthersPriceView();
	
	DeviceTemplatePriceView getDeviceTemplatePriceView();
	
	
  InstanceView getInstanceView( );
	  
	NodeCtrlView getNodeCtrlView( );
	ClusterCtrlView getClusterCtrlView( );
	WalrusCtrlView getWalrusCtrlView( );
	StorageCtrlView getStorageCtrlView( );
	KeypairView getKeypairView( );
  SecurityGroupView getSecurityGroupView();
  IpPermissionView getIpPermissionView();
	CPUStatView getCPUStatView();
	MemoryStatView getMemoryStatView();
	DiskStatView getDiskStatView();
	HistoryView getHistoryView();
  
}

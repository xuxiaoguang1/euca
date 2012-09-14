package com.eucalyptus.webui.client;

import com.eucalyptus.webui.client.place.ErrorSinkPlace;
import com.eucalyptus.webui.client.place.StartPlace;
import com.eucalyptus.webui.client.service.AwsService;
import com.eucalyptus.webui.client.service.AwsServiceAsync;
import com.eucalyptus.webui.client.service.CmdService;
import com.eucalyptus.webui.client.service.CmdServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusService;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.session.LocalSession;
import com.eucalyptus.webui.client.session.LocalSessionImpl;
import com.eucalyptus.webui.client.session.SessionData;
import com.eucalyptus.webui.client.view.*;
import com.eucalyptus.webui.client.view.DeviceAreaView;
import com.eucalyptus.webui.client.view.DeviceAreaViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.client.view.DeviceBWViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUPriceView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUView;
import com.eucalyptus.webui.client.view.DeviceCPUViewImpl;
import com.eucalyptus.webui.client.view.DeviceCabinetView;
import com.eucalyptus.webui.client.view.DeviceCabinetViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskView;
import com.eucalyptus.webui.client.view.DeviceDiskViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPView;
import com.eucalyptus.webui.client.view.DeviceIPViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.client.view.DeviceMemoryViewImpl;
import com.eucalyptus.webui.client.view.DeviceOthersPriceView;
import com.eucalyptus.webui.client.view.DeviceOthersPriceViewImpl;
import com.eucalyptus.webui.client.view.DeviceRoomView;
import com.eucalyptus.webui.client.view.DeviceRoomViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.client.view.DeviceServerViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateView;
import com.eucalyptus.webui.client.view.DeviceTemplateViewImpl;
import com.eucalyptus.webui.client.view.DeviceVMView;
import com.eucalyptus.webui.client.view.DeviceVMViewImpl;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;

public class ClientFactoryImpl implements ClientFactory {
	private static final Place DEFAULT_PLACE = new StartPlace();
	private static final Place ERROR_PLACE = new ErrorSinkPlace();

	private EventBus mainEventBus = new ResettableEventBus(new SimpleEventBus());
	private PlaceController mainPlaceController = new PlaceController(mainEventBus);
	private ActivityManager mainActivityManager;
	private PlaceHistoryHandler mainPlaceHistoryHandler;
	private Historian mainHistorian = new PlaceHistoryHandler.DefaultHistorian();

	private EventBus lifecycleEventBus = new SimpleEventBus();
	private PlaceController lifecyclePlaceController = new PlaceController(lifecycleEventBus);

	private SessionData sessionData = new SessionData();

	private EucalyptusServiceAsync backendService = GWT.create(EucalyptusService.class);
	private AwsServiceAsync backendAwsService = GWT.create( AwsService.class );
	private CmdServiceAsync backendCmdService = GWT.create( CmdService.class );

	private LoginView loginView;
	private LoadingProgressView loadingProgressView;
	private ShellView shellView;
	private StartView startView;
	private ConfigView configView;
	private LoadingAnimationView loadingAnimationView;
	private ErrorSinkView errorSinkView;
	private AccountView accountView;
	private VmTypeView vmTypeView;
	private ReportView reportView;
	private GroupView groupView;
	private UserView userView;
	private UserAppView userAppView;
	private PolicyView policyView;
	private KeyView keyView;
	private CertView certView;
	private ImageView imageView;
	private ActionResultView actionResultView;
	private InstanceView instanceView;
	private NodeCtrlView nodeCtrlView;
	private ClusterCtrlView clusterCtrlView;
	private WalrusCtrlView walrusCtrlView;
	private StorageCtrlView storageCtrlView;
	private IndividualView individualView;
	private DeviceAreaView deviceAreaView;
	private DeviceRoomView deviceRoomView;
	private DeviceCabinetView deviceCabinetView;
	private DeviceCPUPriceView deviceCPUPriceView;
	private DeviceOthersPriceView deviceOthersPriceView;
	private DeviceTemplatePriceView deviceTemplatePriceView;
	private DeviceServerView deviceServerView;
	private DeviceCPUView deviceCPUView;
	private DeviceMemoryView deviceMemoryView;
	private DeviceDiskView deviceDiskView;
	private DeviceIPView deviceIPView;
	private DeviceVMView deviceVMView;
	private DeviceBWView deviceBWView;
	private DeviceTemplateView deviceTemplateView;
	private UserAppAddView UserAppAddView;
	private KeypairView keyPairView;
	private SecurityGroupView securityGroupView;
	private IpPermissionView ipPermissionView;
	private CPUStatView cpuStatView;
	private MemoryStatView memoryStatView;
	private DiskStatView diskStatView;

	private HistoryView historyView;
	
	// Dialogs
	private ConfirmationView confirmationView;
	private InputView inputView;
	private UserAddView userAddView;
	private GroupAddView groupAddView;
	private AccountAddView accountAddView;
	private GroupListView groupListView;
	private GroupDetailView groupDetailView;
	private GroupAddingUserListView groupAddingUserListView;
	private UploadImageView uploadImageView;
	private RunInstanceView runInstanceView;
	private AreaView areaView;

	// Snippets
	private CloudRegistrationView cloudRegView;
  

	@Override
	public LocalSession getLocalSession() {
		return LocalSessionImpl.Get();
	}

	@Override
	public EucalyptusServiceAsync getBackendService() {
		return backendService;
	}

	@Override
	public EventBus getMainEventBus() {
		return mainEventBus;
	}

	@Override
	public PlaceController getMainPlaceController() {
		return mainPlaceController;
	}

	@Override
	public EventBus getLifecycleEventBus() {
		return lifecycleEventBus;
	}

	@Override
	public PlaceController getLifecyclePlaceController() {
		return lifecyclePlaceController;
	}

	@Override
	public LoginView getLoginView() {
		if (loginView == null) {
			loginView = new LoginViewImpl();
		}
		return loginView;
	}

	@Override
	public ShellView getShellView() {
		if (shellView == null) {
			shellView = new ShellViewImpl();
		}
		return shellView;
	}

	@Override
	public StartView getStartView() {
		if (startView == null) {
			startView = new StartViewImpl();
		}
		return startView;
	}

	@Override
	public LoadingProgressView getLoadingProgressView() {
		if (loadingProgressView == null) {
			loadingProgressView = new LoadingProgressViewImpl();
		}
		return loadingProgressView;
	}

	@Override
	public ConfigView getConfigView() {
		if (configView == null) {
			configView = new ConfigViewImpl();
		}
		return configView;
	}

	@Override
	public LoadingAnimationView getLoadingAnimationView() {
		if (loadingAnimationView == null) {
			loadingAnimationView = new LoadingAnimationViewImpl();
		}
		return loadingAnimationView;
	}

	@Override
	public ErrorSinkView getErrorSinkView() {
		if (errorSinkView == null) {
			errorSinkView = new ErrorSinkViewImpl();
		}
		return errorSinkView;
	}

	@Override
	public AccountView getAccountView() {
		if (accountView == null) {
			accountView = new AccountViewImpl();
		}
		return accountView;
	}

	@Override
	public VmTypeView getVmTypeView() {
		if (vmTypeView == null) {
			vmTypeView = new VmTypeViewImpl();
		}
		return vmTypeView;
	}

	@Override
	public ReportView getReportView() {
		if (reportView == null) {
			reportView = new ReportViewImpl();
		}
		return reportView;
	}

	@Override
	public ActivityManager getMainActivityManager() {
		if (mainActivityManager == null) {
			ActivityMapper activityMapper = new MainActivityMapper(this);
			mainActivityManager = new ActivityManager(activityMapper, mainEventBus);
		}
		return mainActivityManager;
	}

	@Override
	public PlaceHistoryHandler getMainPlaceHistoryHandler() {
		if (mainPlaceHistoryHandler == null) {
			MainPlaceHistoryMapper historyMapper = GWT.create(MainPlaceHistoryMapper.class);
			mainPlaceHistoryHandler = new ExPlaceHistoryHandler(historyMapper);
			((ExPlaceHistoryHandler)mainPlaceHistoryHandler).register(mainPlaceController, mainEventBus, DEFAULT_PLACE,
			        ERROR_PLACE);
		}
		return mainPlaceHistoryHandler;
	}

	@Override
	public Historian getMainHistorian() {
		return mainHistorian;
	}

	@Override
	public Place getDefaultPlace() {
		return DEFAULT_PLACE;
	}

	@Override
	public Place getErrorPlace() {
		return ERROR_PLACE;
	}

	@Override
	public SessionData getSessionData() {
		return sessionData;
	}

	@Override
	public GroupView getGroupView() {
		if (groupView == null) {
			groupView = new GroupViewImpl();
		}
		return groupView;
	}

	@Override
	public UserView getUserView() {
		if (userView == null) {
			userView = new UserViewImpl();
		}
		return userView;
	}

	@Override
	public PolicyView getPolicyView() {
		if (policyView == null) {
			policyView = new PolicyViewImpl();
		}
		return policyView;
	}

	@Override
	public KeyView getKeyView() {
		if (keyView == null) {
			keyView = new KeyViewImpl();
		}
		return keyView;
	}

	@Override
	public CertView getCertView() {
		if (certView == null) {
			certView = new CertViewImpl();
		}
		return certView;
	}

	@Override
	public ImageView getImageView() {
		if (imageView == null) {
			imageView = new ImageViewImpl();
		}
		return imageView;
	}

	@Override
	public ConfirmationView getConfirmationView() {
		if (confirmationView == null) {
			confirmationView = new ConfirmationViewImpl();
		}
		return confirmationView;
	}

	@Override
	public InputView getInputView() {
		if (inputView == null) {
			inputView = new InputViewImpl();
		}
		return inputView;
	}

	@Override
	public ActionResultView getActionResultView() {
		if (actionResultView == null) {
			actionResultView = new ActionResultViewImpl();
		}
		return actionResultView;
	}

	@Override
	public ItemView createItemView() {
		return new ItemViewImpl();
	}

	@Override
	public CloudRegistrationView getCloudRegistrationView() {
		if (cloudRegView == null) {
			cloudRegView = new CloudRegistrationViewImpl();
		}
		return cloudRegView;
	}

	@Override
	public UserAddView getUserAddView() {
		// TODO Auto-generated method stub
		if (userAddView == null) {
			userAddView = new UserAddViewImpl();
		}
		return userAddView;
	}

	@Override
	public GroupAddView getGroupAddView() {
		// TODO Auto-generated method stub
		if (groupAddView == null) {
			groupAddView = new GroupAddViewImpl();
		}
		return groupAddView;
	}	

	@Override
	public AccountAddView getAccountAddView() {
		// TODO Auto-generated method stub
		if (accountAddView == null) {
			accountAddView = new AccountAddViewImpl();
		}
		return accountAddView;
	}

	@Override
	public GroupListView getGroupListView() {
		// TODO Auto-generated method stub
		if (groupListView == null) {
			groupListView = new GroupListViewImpl();
		}
		return groupListView;
	}

	@Override
	public GroupDetailView getGroupDetailView() {
		// TODO Auto-generated method stub
		if (groupDetailView == null) {
			groupDetailView = new GroupDetailViewImpl();
		}
		return groupDetailView;
	}

	@Override
	public GroupAddingUserListView getGroupAddingUserListView() {
		// TODO Auto-generated method stub
		if (groupAddingUserListView == null) {
			groupAddingUserListView = new GroupAddingUserListViewImpl();
		}
		return groupAddingUserListView;
	}

	@Override
	public IndividualView getIndividualView() {
		// TODO Auto-generated method stub
		if (individualView == null) {
			individualView = new IndividualViewImpl();
		}
		return individualView;
	}
	
	@Override
	public DeviceAreaView getDeviceAreaView() {
		if (deviceAreaView == null) {
			deviceAreaView = new DeviceAreaViewImpl();
		}
		return deviceAreaView;
	}
	
	@Override
	public DeviceRoomView getDeviceRoomView() {
		if (deviceRoomView == null) {
			deviceRoomView = new DeviceRoomViewImpl();
		}
		return deviceRoomView;
	}
	
	@Override
	public DeviceCabinetView getDeviceCabinetView() {
		if (deviceCabinetView == null) {
			deviceCabinetView = new DeviceCabinetViewImpl();
		}
		return deviceCabinetView;
	}
	
	@Override
	public DeviceCPUPriceView getDeviceCPUPriceView() {
		if (deviceCPUPriceView == null) {
			deviceCPUPriceView = new DeviceCPUPriceViewImpl();
		}
		return deviceCPUPriceView;
	}
	
	@Override
	public DeviceTemplatePriceView getDeviceTemplatePriceView() {
	    if (deviceTemplatePriceView == null) {
	        deviceTemplatePriceView = new DeviceTemplatePriceViewImpl();
	    }
	    return deviceTemplatePriceView;
	}
	
	@Override
	public DeviceOthersPriceView getDeviceOthersPriceView() {
	    if (deviceOthersPriceView == null) {
	        deviceOthersPriceView = new DeviceOthersPriceViewImpl();
	    }
	    return deviceOthersPriceView;
	}
	
	@Override
	public DeviceServerView getDeviceServerView() {
		if (deviceServerView == null) {
			deviceServerView = new DeviceServerViewImpl();
		}
		return deviceServerView;
	}

	@Override
	public DeviceCPUView getDeviceCPUView() {
		if (deviceCPUView == null) {
			deviceCPUView = new DeviceCPUViewImpl();
		}
		return deviceCPUView;
	}

	@Override
	public DeviceMemoryView getDeviceMemoryView() {
		if (deviceMemoryView == null) {
			deviceMemoryView = new DeviceMemoryViewImpl();
		}
		return deviceMemoryView;
	}

	@Override
	public DeviceDiskView getDeviceDiskView() {
		if (deviceDiskView == null) {
			deviceDiskView = new DeviceDiskViewImpl();
		}
		return deviceDiskView;
	}

	@Override
	public DeviceIPView getDeviceIPView() {
		if (deviceIPView == null) {
			deviceIPView = new DeviceIPViewImpl();
		}
		return deviceIPView;
	}
	
	@Override
	public DeviceVMView getDeviceVMView() {
		if (deviceVMView == null) {
			deviceVMView = new DeviceVMViewImpl();
		}
		return deviceVMView;
	}

  @Override
  public AwsServiceAsync getBackendAwsService( ) {
    return backendAwsService;
  }
  
  @Override
  public CmdServiceAsync getBackendCmdService() {
  	return backendCmdService;
  }
  
  @Override
  public InstanceView getInstanceView() {
	if (instanceView == null) {
		instanceView = new InstanceViewImpl( );
	}
	return instanceView;
  }


  @Override
  public NodeCtrlView getNodeCtrlView() {
	if (nodeCtrlView == null) {
		nodeCtrlView = new NodeCtrlViewImpl( );
	}
	return nodeCtrlView;
  }

  @Override
  public ClusterCtrlView getClusterCtrlView() {
	if (clusterCtrlView == null) {
		clusterCtrlView = new ClusterCtrlViewImpl( );
	}
	return clusterCtrlView;
  }
  @Override
  public WalrusCtrlView getWalrusCtrlView() {
	if (walrusCtrlView == null) {
		walrusCtrlView = new WalrusCtrlViewImpl( );
	}
	return walrusCtrlView;
  }
  @Override
  public StorageCtrlView getStorageCtrlView() {
	if (storageCtrlView == null) {
		storageCtrlView = new StorageCtrlViewImpl( );
	}
	return storageCtrlView;
  }
  @Override
	public DeviceBWView getDeviceBWView() {
		if (deviceBWView == null) {
			deviceBWView = new DeviceBWViewImpl();
		}
		return deviceBWView;
	}
       
	@Override
	public DeviceTemplateView getDeviceTemplateView() {
		if (deviceTemplateView == null) {
			deviceTemplateView = new DeviceTemplateViewImpl();
		}
		return deviceTemplateView;
	}

  @Override
  public UploadImageView createUploadImageView() {
    if (uploadImageView == null) {
      uploadImageView = new UploadImageViewImpl();
    }
    return uploadImageView;
  }
  
  @Override
  public UserAppView getUserAppView() {
	// TODO Auto-generated method stub
	if (userAppView == null) {
		userAppView = new UserAppViewImpl();
	}
	return userAppView;
  }

@Override
public UserAppAddView getUserAppAddView() {
	// TODO Auto-generated method stub
	if (UserAppAddView == null) {
		UserAppAddView = new UserAppAddViewImpl();
	}
	return UserAppAddView;
}

  @Override
  public RunInstanceView createRunInstanceView() {
    if (runInstanceView == null) {
      runInstanceView = new RunInstanceViewImpl();
    }
    return runInstanceView;
  }

  @Override
  public KeypairView getKeypairView() {
    if (keyPairView == null) {
      keyPairView = new KeypairViewImpl();
    }
    return keyPairView;
  }

  @Override
  public AreaView createAreaView() {
    if (areaView == null) {
      areaView = new AreaViewImpl();
    }
    return areaView;
  }

  @Override
  public SecurityGroupView getSecurityGroupView() {
    if (securityGroupView == null) {
      securityGroupView = new SecurityGroupViewImpl();
    }
    return securityGroupView;
  }
	@Override
	public CPUStatView getCPUStatView() {
		if (cpuStatView == null) {
			cpuStatView = new CPUStatViewImpl();
		}
		return cpuStatView;
	}

	@Override
	public MemoryStatView getMemoryStatView() {
		if (memoryStatView == null) {
			memoryStatView = new MemoryStatViewImpl();
		}
		return memoryStatView;
	}

	@Override
	public DiskStatView getDiskStatView() {
		if (diskStatView == null) {
			diskStatView = new DiskStatViewImpl();
		}
		return diskStatView;
	}

	@Override
	public HistoryView getHistoryView() {
		if(historyView == null){
			historyView = new HistoryViewImpl();
		}
		return historyView;
	}

  @Override
  public IpPermissionView getIpPermissionView() {
    if (ipPermissionView == null) {
      ipPermissionView = new IpPermissionViewImpl();
    }
    return ipPermissionView;
  }
}

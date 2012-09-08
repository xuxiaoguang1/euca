package com.eucalyptus.webui.client.activity;

import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.IndividualPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.IndividualView;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class IndividualActivity extends AbstractActivity implements IndividualView.Presenter {

	private ClientFactory clientFactory;
	private AcceptsOneWidget container;
	private IndividualPlace place;
	
	private String title;
	
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return this.title;
	}
	
	public IndividualActivity(IndividualPlace place, ClientFactory clientFactory) {
		// TODO Auto-generated constructor stub
		this.place = place;
		this.clientFactory = clientFactory;
	}
	
	@Override
	public void start( AcceptsOneWidget container, EventBus eventBus ) {
		this.container = container;
	    this.clientFactory.getShellView( ).getContentView( ).setContentTitle( getTitle( ) );
	    
	    // Show IndividualView
	    IndividualView view = this.clientFactory.getIndividualView();
	    view.setPresenter(this);
	    this.container.setWidget( view );
	      
	    ActivityUtil.updateDirectorySelection( clientFactory );
	    
	    this.clientFactory.getBackendService().getLoginUserProfile(
	    										this.clientFactory.getLocalSession().getSession(), 
	    										new AsyncCallback<LoginUserProfile> () {

													@Override
													public void onFailure(Throwable caught) {
														// TODO Auto-generated method stub
														
													}

													@Override
													public void onSuccess(LoginUserProfile result) {
														// TODO Auto-generated method stub
														clientFactory.getIndividualView().setLoginUserProfile(result);
													}});
	}

	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_USER_SUCCEED = {"Modify individual info", "修改个人信息成功"};
	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_USER_FAIL = {"Failed to modify individual info", "修改个人信息失败"};
	
	@Override
	public void onUpdateInfo(String title, String mobile, String email) {
		// TODO Auto-generated method stub
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		this.clientFactory.getBackendService().modifyIndividual(this.clientFactory.getLocalSession().getSession(), 
																title, mobile, email, 
																new AsyncCallback<LoginUserProfile> () 
																{

																	@Override
																	public void onFailure(Throwable caught) {
																		// TODO Auto-generated method stub
																		clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, 
																																INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_USER_FAIL[lan], 
																																FooterView.DEFAULT_STATUS_CLEAR_DELAY);
																	}

																	@Override
																	public void onSuccess(LoginUserProfile profile) {
																		// TODO Auto-generated method stub
																		clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, 
																																INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_USER_SUCCEED[lan], 
																																FooterView.DEFAULT_STATUS_CLEAR_DELAY);
																	}
																});
	}

	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_NEW_PWD_EMPTY_WARNING = {"Empty PWD", "密码不能为空"};
	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_NEW_PWD_NOT_EQUAL_WARNING = {"PWD not equal", "两次密码不一致"};
	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_PWD_SUCCEED = {"Modify PWD", "修改个人密码成功"};
	private final String[] INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_PWD_FAIL = {"Failed to modify PWD", "修改个人密码失败"};
	
	@Override
	public void onChangePwd(String oldPwd, String newPwd, String newPwdAgain) {
		// TODO Auto-generated method stub
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		if (oldPwd.length() ==0  || newPwd.length() == 0) {
			clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, 
																	INDIVIDUAL_ACTIVITY_FOOTERVIEW_NEW_PWD_EMPTY_WARNING[lan], 
																	FooterView.DEFAULT_STATUS_CLEAR_DELAY);
		}
		else if (!newPwd.equals(newPwdAgain)) {
			clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, 
																	INDIVIDUAL_ACTIVITY_FOOTERVIEW_NEW_PWD_NOT_EQUAL_WARNING[lan], 
																	FooterView.DEFAULT_STATUS_CLEAR_DELAY);
		}
		else {
			clientFactory.getBackendService().changePassword(this.clientFactory.getLocalSession().getSession(), 
															oldPwd, newPwd, null,
															new AsyncCallback<Void>() {
																@Override
																public void onFailure(Throwable caught) {
																	// TODO Auto-generated method stub
																	clientFactory.getShellView().getFooterView().showStatus(StatusType.ERROR, 
																															INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_PWD_FAIL[lan], 
																															FooterView.DEFAULT_STATUS_CLEAR_DELAY);
																}
																@Override
																public void onSuccess(Void result) {
																	// TODO Auto-generated method stub
																	clientFactory.getShellView().getFooterView().showStatus(StatusType.NONE, 
																															INDIVIDUAL_ACTIVITY_FOOTERVIEW_MODIFY_PWD_SUCCEED[lan], 
																															FooterView.DEFAULT_STATUS_CLEAR_DELAY);
																	clientFactory.getIndividualView().clearPwd();
																}
															});
		}
	}
	
	private static final Logger LOG = Logger.getLogger( IndividualActivity.class.getName( ) );
}

package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.ApplyPlace;
import com.eucalyptus.webui.client.place.LoginPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.view.ActionResultView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.ActionResultView.ResultType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ApplyActivity extends AbstractActivity implements InputView.Presenter, ActionResultView.Presenter {

  private ApplyPlace place;
  private ClientFactory clientFactory;

  public static final String[] PASSWORD_RESET_CAPTION = {"Request password reset", "重置密码"};
  public static final String[] PASSWORD_RESET_SUBJECT = {"Enter user information for password reset:", "请输入用户信息来重置密码"};
  
  public static final String[] RESET_USER_INPUT_TITLE = {"User name", "姓名"};
  public static final String[] RESET_USER_ACCOUNT_INPUT_TITLE = {"User's account name", "账户"};
  
  public static final String[] PASSWORD_INPUT_TITLE = {"Password", "密码"};
  public static final String[] PASSWORD2_INPUT_TITLE = {"Type password again", "请重新输入"};
  public static final String[] EMAIL_INPUT_TITLE = {"Email", "电子邮件"};
  
  public static final String[] PASSWORD_RESET_FAILURE_MESSAGE = {"Failed to complete password reset. Please contact your system administrator.", "重置密码失败，请联系系统管理员"};
  public static final String[] PASSWORD_RESET_SUCCESS_MESSAGE = {"Password reset request is sent. Please check your email for further instructions to change your password.", "密码重置请求已经发送，请查收邮件来完成密码重置"};

  public ApplyActivity( ApplyPlace place, ClientFactory clientFactory ) {
    this.place = place;
    this.clientFactory = clientFactory;
  }
  
  @Override
  public void start( AcceptsOneWidget container, EventBus eventBus ) {
    ActionResultView view = clientFactory.getActionResultView( );
    // clear up
    view.display( ResultType.NONE, "", false );
    view.setPresenter( this );
    container.setWidget( view );
    
    switch ( place.getType( ) ) {
      case PASSWORD_RESET:
        showPasswordResetDialog( );
        break;
      default:
        cancel( null );
        break;
    }
  }

  private void showPasswordResetDialog( ) {
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( PASSWORD_RESET_CAPTION[1], PASSWORD_RESET_SUBJECT[1], new ArrayList<InputField>( Arrays.asList( 
    		new InputField( ) {
			      @Override
			      public String getTitle( ) {
			        return RESET_USER_ACCOUNT_INPUT_TITLE[1];
			      }
		
			      @Override
			      public ValueType getType( ) {
			        return ValueType.TEXT;
			      }
		
			      @Override
			      public ValueChecker getChecker( ) {
			        return ValueCheckerFactory.createAccountNameChecker( );
			      }
		      
		    }, 
		    
		    new InputField( ) {
			      @Override
			      public String getTitle( ) {
			        return RESET_USER_INPUT_TITLE[1];
			      }
			
			      @Override
			      public ValueType getType( ) {
			        return ValueType.TEXT;
			      }
			
			      @Override
			      public ValueChecker getChecker( ) {
			        return ValueCheckerFactory.createUserAndGroupNameChecker( );
			      }
		    }, 
		    new InputField( ) {

			      @Override
			      public String getTitle( ) {
			        return EMAIL_INPUT_TITLE[1];
			      }
			
			      @Override
			      public ValueType getType( ) {
			        return ValueType.TEXT;
			      }
			
			      @Override
			      public ValueChecker getChecker( ) {
			        return ValueCheckerFactory.createEmailChecker( );
			      }
			} 
	)));
  }

  @Override
  public void process( String subject, ArrayList<String> values ) {
    if ( PASSWORD_RESET_SUBJECT[1].equals( subject ) ) {
      doResetPassword( values.get( 0 ), values.get( 1 ), values.get( 2 ) );
    }
  }

  private void doResetPassword( String userName, String accountName, String email ) {
    clientFactory.getActionResultView( ).loading( );
    
    clientFactory.getBackendService( ).requestPasswordRecovery( userName, accountName, email, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
    	  EucalyptusServiceException exception = (EucalyptusServiceException)caught;
        clientFactory.getActionResultView( ).display( ResultType.ERROR, exception.getMessage(), true );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getActionResultView( ).display( ResultType.INFO, PASSWORD_RESET_SUCCESS_MESSAGE[1], true );
      }
      
    } );
  }

  @Override
  public void cancel( String subject ) {
    clientFactory.getLifecyclePlaceController( ).goTo( new LoginPlace( LoginPlace.DEFAULT_PROMPT ) );
  }

  @Override
  public void onConfirmed( ) {
    // Go back
    cancel( null );
  }
  
}

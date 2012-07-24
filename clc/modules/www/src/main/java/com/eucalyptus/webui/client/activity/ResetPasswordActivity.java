package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.LoginPlace;
import com.eucalyptus.webui.client.place.ResetPasswordPlace;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.ActionResultView.ResultType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ResetPasswordActivity extends AbstractActionActivity implements InputView.Presenter {
  
  public static final String RESET_PASSWORD_CAPTION[] = {"Reset password", "重置密码"};
  public static final String RESET_PASSWORD_SUBJECT[] = {"Please enter your new password", "请输入新密码"};
  public static final String PASSWORD_INPUT_TITLE[] = {"Password", "密码"};
  public static final String PASSWORD2_INPUT_TITLE[] = {"Type password again", "请再输入一遍"};
  
  public static final String RESET_FAILURE_MESSAGE[] = {"Can not reset your password. Please contact system administrator.", "重置密码失败，请联系系统管理员"};
  public static final String RESET_SUCCESS_MESSAGE[] = {"Your password is successfully reset. You can proceed to login.", "密码重置成功"};
  
  private String confirmationCode;
  
  public ResetPasswordActivity( ResetPasswordPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doAction( String confirmationCode ) {
    this.confirmationCode = confirmationCode;
    
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( RESET_PASSWORD_CAPTION[1], RESET_PASSWORD_SUBJECT[1], new ArrayList<InputField>( Arrays.asList( new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType( ) {
        return ValueType.NEWPASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createPasswordChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return PASSWORD2_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType( ) {
        return ValueType.PASSWORD;
      }

      @Override
      public ValueChecker getChecker( ) {
        return null;
      }
      
    } ) ) );
  }

  @Override
  public void process( String subject, ArrayList<String> values ) {
    if ( RESET_PASSWORD_SUBJECT[1].equals( subject ) ) {
      doResetPassword( values.get( 0 ) );
    }
  }

  private void doResetPassword( String password ) {
    clientFactory.getActionResultView( ).loading( );
    
    clientFactory.getBackendService( ).resetPassword( confirmationCode, password, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        clientFactory.getActionResultView( ).display( ResultType.ERROR, RESET_FAILURE_MESSAGE[1], true );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getActionResultView( ).display( ResultType.INFO, RESET_SUCCESS_MESSAGE[1], true );
      }
      
    } );
  }

  // Click on the button on the result page
  @Override
  public void onConfirmed( ) {
    cancel( null );
  }
  
  // Click on the cancel button on the reset password dialog
  @Override
  public void cancel( String subject ) {
    // Make sure we don't bring the confirm action URL into main interface.
    History.newItem( "" );
    clientFactory.getLifecyclePlaceController( ).goTo( new LoginPlace( LoginPlace.DEFAULT_PROMPT ) );
  }
  
}

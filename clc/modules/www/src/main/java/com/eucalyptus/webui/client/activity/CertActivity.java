package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.CertView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CertActivity extends AbstractSearchActivity implements CertView.Presenter, ConfirmationView.Presenter {

	public static final String TITLE = "X509 证书";

	public static final String[] DELETE_CERT_CAPTION = {"Delete selected certificate", "删除选中的证书"};
	public static final String[] DELETE_CERT_SUBJECT = {"Are you sure you want to delete the following selected certificate?", "你确定要删除选中的证书么？"};
	
	public static final String[] ACTIVATE_CERT_CAPTION = {"", "激活选中的证书"};
	public static final String[] ACTIVATE_CERT_SUBJECT = {"", "你确定要激活选中的证书么？"};
	
	public static final String[] DEACTIVATE_CERT_CAPTION = {"", "停止选中的证书"};
	public static final String[] DEACTIVATE_CERT_SUBJECT = {"", "你确定要停止选中的证书么？"};
	
	public static final String[] REVOKE_CERT_CAPTION = {"", "撤销选中的证书"};
	public static final String[] REVOKE_CERT_SUBJECT = {"", "你确定要撤销选中的证书么？"};
	
	public static final String[] AUTHORIZE_CERT_CAPTION = {"", "授权选中的证书"};
	public static final String[] AUTHORIZE_CERT_SUBJECT = {"", "你确定要授权选中的证书么？"};

	private static final Logger LOG = Logger.getLogger(CertActivity.class.getName());

	private Set<SearchResultRow> currentSelected;

	public CertActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		this.clientFactory.getBackendService().lookupCertificate(
			this.clientFactory.getLocalSession().getSession(), search,range, 
				new AsyncCallback<SearchResult>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory, caught);
						LOG.log(Level.WARNING, "Search failed: " + caught);
						displayData(null);
					}

					@Override
					public void onSuccess(SearchResult result) {
						LOG.log(Level.INFO, "Search success:" + result);
						displayData(result);
					}

				});
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selection) {
		this.currentSelected = selection;
		if (selection == null || selection.size() != 1) {
			LOG.log(Level.INFO, "Not a single selection");
			this.clientFactory.getShellView().hideDetail();
		} else {
			LOG.log(Level.INFO, "Selection changed to " + selection);
			this.clientFactory.getShellView().showDetail(DETAIL_PANE_SIZE);
			showSingleSelectedDetails(selection.toArray(new SearchResultRow[0])[0]);
		}
	}

	@Override
	public void saveValue(ArrayList<String> keys,ArrayList<HasValueWidget> values) {
	}

	@Override
	protected String getTitle() {
		return TITLE;
	}

	@Override
	protected void showView(SearchResult result) {
		if (this.view == null) {
			this.view = this.clientFactory.getCertView();
			((CertView) this.view).setPresenter(this);
			container.setWidget(this.view);
			((CertView) this.view).clear();
		}
		((CertView) this.view).showSearchResult(result);
	}

	@Override
	public void confirm(String subject) {
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		if(DELETE_CERT_SUBJECT[lan].equals(subject)){
			doDeleteCert();
		}else if(ACTIVATE_CERT_SUBJECT[lan].equals(subject)){
			doActivateCert();
		}else if(DEACTIVATE_CERT_SUBJECT[lan].equals(subject)){
			doDeactivateCert();
		}else if(REVOKE_CERT_SUBJECT[lan].equals(subject)){
			doRevokeCert();
		}else if(AUTHORIZE_CERT_SUBJECT[lan].equals(subject)){
			doAuthorizeCert();
		}
	}
	

	private void doDeleteCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			return;
		}

		final ArrayList<String> ids = Lists.newArrayList();
		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "正在删除证书...", 0);

		clientFactory.getBackendService().deleteCertificate(clientFactory.getLocalSession().getSession(), ids,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "删除失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "删除失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "证书已删除", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "证书已删除");
						reloadCurrentRange();
					}
		 } );
	}
	
	private void doActivateCert(){
		if (currentSelected == null || currentSelected.size() == 0) {
			return;
		}

		final ArrayList<String> ids = Lists.newArrayList();
		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "正在激活证书...", 0);

		clientFactory.getBackendService().modifyCertificate(clientFactory.getLocalSession().getSession(), ids, true, null, 
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "激活失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "激活失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "证书已激活", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "证书已激活");
						reloadCurrentRange();
					}
		 } );
	}

	private void doDeactivateCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			return;
		}

		final ArrayList<String> ids = Lists.newArrayList();
		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "正在停止证书...", 0);

		clientFactory.getBackendService().modifyCertificate(clientFactory.getLocalSession().getSession(), ids, false, null, 
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "停止失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "停止失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "证书已停止", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "证书已停止");
						reloadCurrentRange();
					}
		 } );
	}

	private void doRevokeCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			return;
		}

		final ArrayList<String> ids = Lists.newArrayList();
		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "正在撤销证书...", 0);

		clientFactory.getBackendService().modifyCertificate(clientFactory.getLocalSession().getSession(), ids, null, true, 
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "撤销失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "撤销失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "证书已撤销", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "证书已撤销");
						reloadCurrentRange();
					}
		 } );
	}

	private void doAuthorizeCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			return;
		}

		final ArrayList<String> ids = Lists.newArrayList();
		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "正在授权证书...", 0);

		clientFactory.getBackendService().modifyCertificate(clientFactory.getLocalSession().getSession(), ids, null, false, 
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "授权失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "授权失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "证书已授权", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "证书已授权");
						reloadCurrentRange();
					}
		 } );
	}
	

	@Override
	public void onDeleteCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR, "至少选中一个要删除的证书", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return;
		}
		ConfirmationView dialog = this.clientFactory.getConfirmationView();
		dialog.setPresenter(this);
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(DELETE_CERT_CAPTION[lan], DELETE_CERT_SUBJECT[lan]);
	}

	@Override
	public void onActivateCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR, "至少选中一个要激活的证书", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return;
		}
		ConfirmationView dialog = this.clientFactory.getConfirmationView();
		dialog.setPresenter(this);
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(ACTIVATE_CERT_CAPTION[lan], ACTIVATE_CERT_SUBJECT[lan]);
	}

	@Override
	public void onDeactivateCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR, "至少选中一个要停止的证书", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return;
		}
		ConfirmationView dialog = this.clientFactory.getConfirmationView();
		dialog.setPresenter(this);
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(DEACTIVATE_CERT_CAPTION[lan], DEACTIVATE_CERT_SUBJECT[lan]);
	}

	@Override
	public void onRevokeCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR, "至少选中一个要撤销的证书", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return;
		}
		ConfirmationView dialog = this.clientFactory.getConfirmationView();
		dialog.setPresenter(this);
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(REVOKE_CERT_CAPTION[lan], REVOKE_CERT_SUBJECT[lan]);
	}

	@Override
	public void onAuthorizeCert() {
		if (currentSelected == null || currentSelected.size() == 0) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR, "至少选中一个要授权的证书", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return;
		}
		ConfirmationView dialog = this.clientFactory.getConfirmationView();
		dialog.setPresenter(this);
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(AUTHORIZE_CERT_CAPTION[lan], AUTHORIZE_CERT_SUBJECT[lan]);
	}

}

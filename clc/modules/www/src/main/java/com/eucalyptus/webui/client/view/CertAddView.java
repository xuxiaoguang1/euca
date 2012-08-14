package com.eucalyptus.webui.client.view;

public interface CertAddView {
	
	void display(String caption);

	void setPresenter(Presenter presenter);

	public interface Presenter {
		void processAddCert(String pem);
	}
}

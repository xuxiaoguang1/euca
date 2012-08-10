package com.eucalyptus.webui.client.view;

import java.util.List;

import com.eucalyptus.webui.shared.aws.ImageType;

public interface UploadImageView {
	void setFocus( );
	void display();
	
	void setPresenter( Presenter presenter );
	public interface Presenter {
	    void processImage(String file, ImageType type, String bucket, String name, String kernel, String ramdisk);
	    List<String> getKernels();
	    List<String> getRamDisks();
	}
}

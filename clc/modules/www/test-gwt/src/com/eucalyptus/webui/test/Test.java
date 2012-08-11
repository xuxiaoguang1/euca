package com.eucalyptus.webui.test;

import java.security.cert.X509Certificate;

import com.eucalyptus.webui.server.auth.crypto.Crypto;
import com.eucalyptus.webui.server.auth.util.B64;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;
import com.eucalyptus.webui.shared.auth.Certificate;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Crypto.generateQueryId());
		System.out.println(Crypto.generateSecretKey());
		
		String userId = "inno";
		String pem = "maybeaninvalidpem";
		String encodedPem = B64.url.encString(pem);
		
		System.out.println("Ecoded Pem:" + encodedPem);
		
		X509Certificate x509 = X509CertHelper.toCertificate(encodedPem);
		if(x509 == null){
			throw new RuntimeException("Invalid cert");
		}
		Certificate cert = new Certificate(userId, X509CertHelper.fromCertificate(x509));
		
		System.out.println(cert.getCertificateId());
		System.out.println(cert.getPem());
	}

}

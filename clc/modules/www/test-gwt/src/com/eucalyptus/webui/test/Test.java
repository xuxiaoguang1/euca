package com.eucalyptus.webui.test;

import java.security.KeyPair;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import com.eucalyptus.webui.server.auth.crypto.Certs;
import com.eucalyptus.webui.server.auth.crypto.Crypto;
import com.eucalyptus.webui.server.auth.util.B64;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;
import com.eucalyptus.webui.shared.auth.Certificate;

public class Test {
	public static final String ppp = "" +
			"MIIDFTCCAf2gAwIBAgIGG5pu8KqIMA0GCSqGSIb3DQEBDQUAMEExCzAJBgNVBAYT" +
			"AlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYDVQQD" +
			"EwVhZG1pbjAeFw0xMjA3MDExNDI5MTVaFw0xNzA3MDExNDI5MTVaMEExCzAJBgNV" +
			"BAYTAlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYD" +
			"VQQDEwVhZG1pbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJDO+2dO" +
			"7VspFEllQWhEgrpcD3yOpBDNEMXlNrlKngJd6F6YfsvNQmDfjgJ/kYaCO1LA4Kbq" +
			"zoWWm3DGv6zD+SQYTFHE+g3ziWOLEtEdI3oioG0xAnN/aRB0OJsc143/uPvXaKgh" +
			"I4niGWGD+Ay9386zF7ydcMSACdK8MEDllYG7sVUUkLWQwcP4nGtRlj2aj6CEOyWw" +
			"gDPfMRZ1Hbj6DR7LQaVHjuSHYqirxjQTGoR6aXJyrd7VoQ8wSDUkj7MGBzNkYoVE" +
			"nzJFeRzWEFP8BE8kGoW7a7bHyhRWE9onBVR2Y4lktD1QIjYJke5eS8zbM17egNQ8" +
			"OTklisjpUB78LMECAwEAAaMTMBEwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0B" +
			"AQ0FAAOCAQEAd03+TCFEMQNBFYKJOK4/6ButKzToDIQjzTC9yl7WYNU29DpyxWny" +
			"0lldJ3fnDMmFTa01+eOlKtgxWHNVUBRTIrPZXZZnbN/slhbBpCwGmS2TOmcMzeNB" +
			"7GSncv/d60cUeyQBQRcyKji1HLvO9ZWzS+BQgzA5IM0SRTFWOu61AoBsM2cnVQSv" +
			"U1NuuUyKYS0s+Ia7PH4rTUdHUOhcCJOtE/rZ87PnwiKWInb4TZv+ucStfO3LFYGH" +
			"Zl2/dW1zvLui+VDmI0JTFWZOnbYMNVNeb1ly+2gQU7Fz8Dkexdepf8Lr+FjyAdIp" +
			"K5sOakkPcDjNhiVQhMBwhV2jorT+y/vglw==" +
			"";
//	public static final String certBody = "-----BEGIN CERTIFICATE-----\n"
//			+ "MIIDFTCCAf2gAwIBAgIGG5pu8KqIMA0GCSqGSIb3DQEBDQUAMEExCzAJBgNVBAYT\n"
//			+ "AlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYDVQQD\n"
//			+ "EwVhZG1pbjAeFw0xMjA3MDExNDI5MTVaFw0xNzA3MDExNDI5MTVaMEExCzAJBgNV\n"
//			+ "BAYTAlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYD\n"
//			+ "VQQDEwVhZG1pbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJDO+2dO\n"
//			+ "7VspFEllQWhEgrpcD3yOpBDNEMXlNrlKngJd6F6YfsvNQmDfjgJ/kYaCO1LA4Kbq\n"
//			+ "zoWWm3DGv6zD+SQYTFHE+g3ziWOLEtEdI3oioG0xAnN/aRB0OJsc143/uPvXaKgh\n"
//			+ "I4niGWGD+Ay9386zF7ydcMSACdK8MEDllYG7sVUUkLWQwcP4nGtRlj2aj6CEOyWw\n"
//			+ "gDPfMRZ1Hbj6DR7LQaVHjuSHYqirxjQTGoR6aXJyrd7VoQ8wSDUkj7MGBzNkYoVE\n"
//			+ "nzJFeRzWEFP8BE8kGoW7a7bHyhRWE9onBVR2Y4lktD1QIjYJke5eS8zbM17egNQ8\n"
//			+ "OTklisjpUB78LMECAwEAAaMTMBEwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0B\n"
//			+ "AQ0FAAOCAQEAd03+TCFEMQNBFYKJOK4/6ButKzToDIQjzTC9yl7WYNU29DpyxWny\n"
//			+ "0lldJ3fnDMmFTa01+eOlKtgxWHNVUBRTIrPZXZZnbN/slhbBpCwGmS2TOmcMzeNB\n"
//			+ "7GSncv/d60cUeyQBQRcyKji1HLvO9ZWzS+BQgzA5IM0SRTFWOu61AoBsM2cnVQSv\n"
//			+ "U1NuuUyKYS0s+Ia7PH4rTUdHUOhcCJOtE/rZ87PnwiKWInb4TZv+ucStfO3LFYGH\n"
//			+ "Zl2/dW1zvLui+VDmI0JTFWZOnbYMNVNeb1ly+2gQU7Fz8Dkexdepf8Lr+FjyAdIp\n"
//			+ "K5sOakkPcDjNhiVQhMBwhV2jorT+y/vglw==\n"
//			+ "-----END CERTIFICATE-----\n";
	
	public static final String certBody = "-----BEGIN CERTIFICATE-----\n" +
"MIIDFTCCAf2gAwIBAgIGG5pu8KqIMA0GCSqGSIb3DQEBDQUAMEExCzAJBgNVBAYT\n" +
"AlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYDVQQD\n" +
"EwVhZG1pbjAeFw0xMjA3MDExNDI5MTVaFw0xNzA3MDExNDI5MTVaMEExCzAJBgNV\n" +
"BAYTAlVTMQ0wCwYDVQQKEwRVc2VyMRMwEQYDVQQLEwpFdWNhbHlwdHVzMQ4wDAYD\n" +
"VQQDEwVhZG1pbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJDO+2dO\n" +
"7VspFEllQWhEgrpcD3yOpBDNEMXlNrlKngJd6F6YfsvNQmDfjgJ/kYaCO1LA4Kbq\n" +
"zoWWm3DGv6zD+SQYTFHE+g3ziWOLEtEdI3oioG0xAnN/aRB0OJsc143/uPvXaKgh\n" +
"I4niGWGD+Ay9386zF7ydcMSACdK8MEDllYG7sVUUkLWQwcP4nGtRlj2aj6CEOyWw\n" +
"gDPfMRZ1Hbj6DR7LQaVHjuSHYqirxjQTGoR6aXJyrd7VoQ8wSDUkj7MGBzNkYoVE\n" +
"nzJFeRzWEFP8BE8kGoW7a7bHyhRWE9onBVR2Y4lktD1QIjYJke5eS8zbM17egNQ8\n" +
"OTklisjpUB78LMECAwEAAaMTMBEwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0B\n" +
"AQ0FAAOCAQEAd03+TCFEMQNBFYKJOK4/6ButKzToDIQjzTC9yl7WYNU29DpyxWny\n" +
"0lldJ3fnDMmFTa01+eOlKtgxWHNVUBRTIrPZXZZnbN/slhbBpCwGmS2TOmcMzeNB\n" +
"7GSncv/d60cUeyQBQRcyKji1HLvO9ZWzS+BQgzA5IM0SRTFWOu61AoBsM2cnVQSv\n" +
"U1NuuUyKYS0s+Ia7PH4rTUdHUOhcCJOtE/rZ87PnwiKWInb4TZv+ucStfO3LFYGH\n" +
"Zl2/dW1zvLui+VDmI0JTFWZOnbYMNVNeb1ly+2gQU7Fz8Dkexdepf8Lr+FjyAdIp\n" +
"K5sOakkPcDjNhiVQhMBwhV2jorT+y/vglw==\n" +
"-----END CERTIFICATE-----\n";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(Crypto.generateQueryId());
//		System.out.println(Crypto.generateSecretKey());
		
//		String userId = "inno";
//		String pem = "maybeaninvalidpem";
		//String encodedPem = B64.url.encString(ppp);
		
//		String dec = B64.decString(ppp);
//		System.out.println("deced: \n" + dec);
		
		
		
		//System.out.println("Ecoded Pem:" + encodedPem);
		
//		X509Certificate x509 = X509CertHelper.toCertificate(dec);
//		if(x509 == null){
//			throw new RuntimeException("Invalid cert");
//		}
//		Certificate cert = new Certificate(userId, X509CertHelper.fromCertificate(x509));
		
//		System.out.println(cert.getCertificateId());
//		System.out.println(cert.getPem());
		
		
//		KeyPair keyPair = Certs.generateKeyPair();
//		System.out.println(keyPair.getPrivate());
//		System.out.println(keyPair.getPublic());
//		
//		X509Certificate x509 = Certs.generateCertificate(keyPair, "eucalyptus");
//		try {
//			x509.checkValidity();
//		} catch (CertificateExpiredException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CertificateNotYetValidException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String encodedPem = B64.url.encString(certBody);
		
		System.out.println("encoded:");
		System.out.println(encodedPem);
		
		System.out.println("decoded:");
		System.out.println(B64.url.decString(encodedPem));
		
		X509Certificate x509 = X509CertHelper.toCertificate(encodedPem);
//		X509Certificate x509 = X509CertHelper.pemToCertificate(encodedPem);
		System.out.println(x509);
	}

}

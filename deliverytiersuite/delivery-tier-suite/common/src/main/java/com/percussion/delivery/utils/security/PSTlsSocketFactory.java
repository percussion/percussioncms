/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.delivery.utils.security;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


public class PSTlsSocketFactory implements SecureProtocolSocketFactory
{

	   public static final Logger log = LogManager.getLogger(PSTlsSocketFactory.class);
	   private  static final String defaultProtocols = "TLSv1.1,TLSv1.2";
	   private final String[] protocols;
	   private SSLSocketFactory internalSSLSocketFactory;
	   private String[] enabledCiphers;
	   private String[] defaultCiphers = {
	         "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
			   "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
			   "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
			   "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
			   "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
			   "TLS_DH_RSA_WITH_AES_128_GCM_SHA256",
			   "TLS_RSA_WITH_AES_128_GCM_SHA256",
			   "TLS_RSA_WITH_AES_256_GCM_SHA384"};
	   
	   public PSTlsSocketFactory(String ciphers) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
	        String protString = System.getProperty("jdk.tls.client.protocols");
	        if (protString==null)
	           protString= System.getProperty("https.protocols");
	        if (protString!=null)
	        {
	           String[] protArr = protString.split(",");
	           protocols = new String[protArr.length];
	           for (int i=0; i< protArr.length; i++)
	           {
	              protocols[i]=protArr[i].trim();
	           }
	        }else{
	        	protocols=defaultProtocols.split(",");
	        }
	        
	        //Work around for Secure Random failing
	        try {
	           Field propsField = Security.class.getDeclaredField("props");
	           propsField.setAccessible(true);
	           Properties props = (Properties) propsField.get(null);
	           props.remove("securerandom.source");
	       } catch(Exception e) {
	          log.warn("Unable to modify java.lang.Security properties!");
	          log.warn("BouncyCastle DRBG may fail.");
	       }
	        
	        Provider[] prov = Security.getProviders();
	        log.debug("--- Configured Providers ---");
	        for(Provider p : prov){
	           log.debug(p.getName());
	        }
			SSLContext context;
			context = SSLContext.getInstance("TLS");


		   log.debug("Initialized TLSSocketFactory enabled protocols to : {}",Arrays.toString(protocols));
	        log.debug("Using Security Provider: {}" , context.getProvider());
	       
	        TrustManagerFactory trustManagerFactory;
		   trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
		   trustManagerFactory.init((KeyStore) null);
	        TrustManager[] tm = trustManagerFactory.getTrustManagers();
	        SecureRandom random;
	    
	         try
	         {
	            random = SecureRandom.getInstance("DEFAULT", "BC");
	         }
	         catch (NoSuchProviderException e)
	         {
	            random = SecureRandom.getInstance("DEFAULT");
	         }
	        context.init(null, tm, random);
	        
	        internalSSLSocketFactory = context.getSocketFactory();
	        
	        
	        if(ciphers == null || StringUtils.isEmpty(ciphers)){
	           enabledCiphers = defaultCiphers;
	        }else {
				enabledCiphers = ciphers.split(",");
			}
			for(String s : enabledCiphers){
	    	   s = s.trim();
	        }

	    }

	    @Override
	    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
	    }

	    @Override
	    public Socket createSocket(String host, int port) throws IOException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
	    }

	    @Override
	    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
	    }

	    private void logTLSConfig(SSLSocket socket){
	       
	       if(log.isDebugEnabled()){
	          try{
	             SSLContext context = SSLContext.getDefault();
	             log.debug("Default TLS Provider is: {}" , context.getProvider().getName());
	          }
	          catch (NoSuchAlgorithmException e) {
				  log.debug("WARNING!  No TLS Providers are available!");
			  }
	          log.debug("--- Enabled Protocols ---");
	       for(String s : socket.getEnabledProtocols()){
	          log.debug("Protocol:{} :ENABLED",s);
	     }
	       log.debug("--- Enabled Cipher Suites ---");
	       for(String s : socket.getEnabledCipherSuites()){
	          log.debug("Cipher: {} :ENABLED",s);
	       }
	     
	       log.debug("--- Supported Cipher Suites ---");
	       for(String s : socket.getSupportedCipherSuites()){
	          log.debug("Cipher: {} :Supported",s);
	       }
	       
	       }
	    
	    }
	    private Socket enableTLSOnSocket(Socket socket) {
	        if(socket instanceof SSLSocket) {
	           SSLSocket sslSocket = (SSLSocket) socket;
	              sslSocket.setEnabledProtocols(protocols);
	            sslSocket.setEnabledCipherSuites(scrubCiphers(sslSocket.getSupportedCipherSuites(), enabledCiphers));
	            log.debug("Setting Enabled Ciphers to: {}" , StringUtils.join(enabledCiphers,","));
	            logTLSConfig(sslSocket);
	        }
	        return socket;
	    }

	private static String[] scrubCiphers(String[] supportedCipherSuites, String[] enabledCiphers) {
		ArrayList<String> ret = new ArrayList<>();
		Collections.addAll(ret, enabledCiphers);
		List<String> copySupported = Arrays.asList(supportedCipherSuites);
		for(String s : enabledCiphers){
			if(!copySupported.contains(s))
				ret.remove(s);
		}
		return ret.toArray(new String[]{});
	}
	   @Override
	   public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
	         throws IOException
	   {
	      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localAddress, localPort));
	   }
}

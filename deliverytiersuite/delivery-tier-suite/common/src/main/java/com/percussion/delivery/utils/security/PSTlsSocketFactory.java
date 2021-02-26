/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.utils.security;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Properties;


public class PSTlsSocketFactory implements SecureProtocolSocketFactory
{

	   public static Log log = LogFactory.getLog(PSTlsSocketFactory.class);
	   private final static String defaultProtocols = "TLSv1.1,TLSv1.2";
	   private final String[] protocols;
	   private SSLSocketFactory internalSSLSocketFactory;
	   private String enabledCiphers[];
	   private String defaultCiphers[] = {
	         "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
	         "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
	         "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
	         "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
	         "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
	         "TLS_RSA_WITH_AES_256_CBC_SHA",
	         "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
	         "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
	         "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
	         "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
	         "TLS_RSA_WITH_AES_128_CBC_SHA",
	         "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
	         "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
	         "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
	         "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
	         "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
	         "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
	         "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
	         "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
	         "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
	         "SSL_RSA_WITH_RC4_128_SHA",
	         "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
	         "TLS_ECDH_RSA_WITH_RC4_128_SHA",
	         "SSL_RSA_WITH_RC4_128_MD5",
	         "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};
	   
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
	      try
	      {
	         context = SSLContext.getInstance("TLS","BCJSSE");
	      }
	      catch (NoSuchProviderException e)
	      {
	         log.warn("Unable to initialize Bouncy Castle TLS provider, not found.  Reverting to default TLS provider.");
	         //use the default
	         context = SSLContext.getInstance("TLS");
	      }
	       
	        
	        log.debug("Initialized TLSSocketFactory enabled protocols to :"+Arrays.toString(protocols));
	        log.debug("Using Security Provider:" + context.getProvider().toString());
	       
	        TrustManagerFactory trustManagerFactory;
	      try
	      {
	         trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
	      }
	      catch (NoSuchProviderException e1)
	      {
	         trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	      }
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
	        
	        
	        if(StringUtils.isEmpty(ciphers)){
	           enabledCiphers = defaultCiphers;
	        }
	        enabledCiphers = ciphers.split(",");
	        for(String s : enabledCiphers){
	           s = s.trim();
	        }
	       
	    }

	    @Override
	    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
	    }

	    @Override
	    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
	    }

	    @Override
	    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
	        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
	    }

	    private void logTLSConfig(SSLSocket socket){
	       
	       if(log.isDebugEnabled()){
	          try{
	             SSLContext context = SSLContext.getDefault();
	             log.debug("Default TLS Provider is: " + context.getProvider().getName());
	          }
	          catch (NoSuchAlgorithmException e)
	          {
	             log.debug("WARNING!  No TLS Providers are available!");
	          }finally{}
	          log.debug("--- Enabled Protocols ---");
	       for(String s : socket.getEnabledProtocols()){
	          log.debug("Protocol: " + s + ":ENABLED");
	     }
	       log.debug("--- Enabled Cipher Suites ---");
	       for(String s : socket.getEnabledCipherSuites()){
	          log.debug("Cipher: " + s + ":ENABLED");
	       }
	     
	       log.debug("--- Supported Cipher Suites ---");
	       for(String s : socket.getSupportedCipherSuites()){
	          log.debug("Cipher: " + s + ":Supported");
	       }
	       
	       }
	    
	    }
	    private Socket enableTLSOnSocket(Socket socket) {
	        if(socket != null && (socket instanceof SSLSocket)) {
	           SSLSocket sslSocket = (SSLSocket) socket;
	              sslSocket.setEnabledProtocols(protocols);
	            sslSocket.setEnabledCipherSuites(enabledCiphers);
	            log.debug("Setting Enabled Ciphers to: " + StringUtils.join(enabledCiphers,","));
	            logTLSConfig(sslSocket);
	        }
	        return socket;
	    }

	   @Override
	   public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
	         throws IOException, UnknownHostException, ConnectTimeoutException
	   {
	      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localAddress, localPort));
	   }
}
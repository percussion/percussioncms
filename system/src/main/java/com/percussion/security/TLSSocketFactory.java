/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.security;

import com.percussion.server.PSServer;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

import javax.net.SocketFactory;
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
import java.util.Arrays;
import java.util.Properties;

public class TLSSocketFactory extends SSLSocketFactory implements ProtocolSocketFactory {
   
    /**
    * Logger for the reaper, never <code>null</code>.
    */
    private static final Logger ms_log = LogManager.getLogger(TLSSocketFactory.class);
   
    private SSLSocketFactory internalSSLSocketFactory;

    private String[] protocols = new String[] {"TLSv1.2","TLSv1.3"};
    private String[] enabledCiphers;
    private String[] defaultCiphers = {
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_DH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_256_GCM_SHA384"};
    
    
    private static SSLSocketFactory instance;

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {


        //Register Bouncy Castle
        Security.addProvider(new BouncyCastleJsseProvider());
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");


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
        }
        
        //Work around for Secure Random failing
        try {
           Field propsField = Security.class.getDeclaredField("props");
           propsField.setAccessible(true);
           Properties props = (Properties) propsField.get(null);
           props.remove("securerandom.source");
       } catch(Exception e) {
          ms_log.warn("Unable to modify java.lang.Security properties!");
          ms_log.warn("BouncyCastle DRBG may fail.");
       }
        
        Provider[] prov = Security.getProviders();
        ms_log.debug("--- Configured Providers ---");
        for(Provider p : prov){
           ms_log.debug(p.getName());
        }
       
      
        SSLContext context;
      try
      {
         context = SSLContext.getInstance("TLS","BCJSSE");
      }
      catch (NoSuchProviderException e)
      {
         ms_log.warn("Unable to initialize Bouncy Castle TLS provider, not found.  Reverting to default TLS provider.");
         //use the default
         context = SSLContext.getInstance("TLS");
      }
       
        
        ms_log.debug("Initialized TLSSocketFactory enabled protocols to : {}",Arrays.toString(protocols));
        ms_log.debug("Using Security Provider: {}" , context.getProvider());
       
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
        
        String ciphers = PSServer.getServerProps().getProperty("enabledCiphers");
        if(StringUtils.isEmpty(ciphers)){
           enabledCiphers = defaultCiphers;
        }
        enabledCiphers = ciphers.split(",");
        for(String s : enabledCiphers){
           s = s.trim();
        }
       
    }

    public static synchronized SocketFactory getDefault() {
        if (instance==null)
        {
            try {
                instance=new TLSSocketFactory();
            } catch (Exception e)
            {
                return SSLSocketFactory.getDefault();
            }
        }
        return instance;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
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

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private void logTLSConfig(SSLSocket socket){
       
       if(ms_log.isDebugEnabled()){
          try{
             SSLContext context = SSLContext.getDefault();
             ms_log.debug("Default TLS Provider is: {}" , context.getProvider().getName());
          }
          catch (NoSuchAlgorithmException e)
          {
             ms_log.debug("WARNING!  No TLS Providers are available!");
          }
          ms_log.debug("--- Enabled Protocols ---");
       for(String s : socket.getEnabledProtocols()){
          ms_log.debug("Protocol: {} :ENABLED",s);
     }
       ms_log.debug("--- Enabled Cipher Suites ---");
       for(String s : socket.getEnabledCipherSuites()){
          ms_log.debug("Cipher: {} :ENABLED",s);
       }
     
       ms_log.debug("--- Supported Cipher Suites ---");
       for(String s : socket.getSupportedCipherSuites()){
          ms_log.debug("Cipher: {} :Supported", s);
       }
       
       }
    
    }
    private Socket enableTLSOnSocket(Socket socket) {
        if((socket instanceof SSLSocket)) {
           SSLSocket sslSocket = (SSLSocket) socket;
              sslSocket.setEnabledProtocols(protocols);
            sslSocket.setEnabledCipherSuites(enabledCiphers);
            ms_log.debug("Setting Enabled Ciphers to: {}" , StringUtils.join(enabledCiphers,","));
            logTLSConfig(sslSocket);
        }
        return socket;
    }

   @Override
   public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
         throws IOException
   {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localAddress, localPort));
   }
}

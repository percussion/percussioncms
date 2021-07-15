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
package com.percussion.delivery.client;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * Implements a socket factory supporting TLSv1.2 and TLSv1.2
 * 
 * @author natechadwick
 *
 */
public class TLSProtocolSocketFactory extends SSLProtocolSocketFactory{

   private final static String defaultProtocols = "TLSv1.1,TLSv1.2";
   private final String[] protocols;
   
   public TLSProtocolSocketFactory() {
      super(); 
      String protocols = System.getProperty("https.protocols", defaultProtocols);
      this.protocols = protocols.split(",");
     }

   /**
    * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
    */
     @Override public Socket createSocket(String host, int port) throws IOException ,UnknownHostException 
     {
        SSLSocket sslSocket = (SSLSocket)super.createSocket(host, port);
        sslSocket.setEnabledProtocols(protocols); 
        return sslSocket;
     }
    
     
     @Override public Socket createSocket(String host, int port, java.net.InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException ,UnknownHostException ,org.apache.commons.httpclient.ConnectTimeoutException {
        SSLSocket sslSocket = (SSLSocket)super.createSocket(host, port, localAddress, localPort, params);
        sslSocket.setEnabledProtocols(protocols);
       return sslSocket;
     }
     
   @Override
   public Socket createSocket(String host, int port, java.net.InetAddress clientHost, int clientPort) throws IOException ,UnknownHostException {
      SSLSocket sslSocket = (SSLSocket)super.createSocket(host,port,clientHost,clientPort);
      sslSocket.setEnabledProtocols(protocols);
      return sslSocket;
   }
   
    /* (non-Javadoc)
    * @see org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
    */
   @Override
   public Socket createSocket(Socket socket, String host, int port,
         boolean autoClose) throws IOException, UnknownHostException
   {
      SSLSocket sslSocket = (SSLSocket)super.createSocket(socket, host, port, autoClose);
      sslSocket.setEnabledProtocols(protocols);
      return sslSocket;
   }
      
}

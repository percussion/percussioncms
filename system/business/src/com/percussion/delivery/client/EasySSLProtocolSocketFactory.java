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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s 
 * that accept self-signed certificates. 
 * </p>
 * <p>
 * This socket factory SHOULD NOT be used for productive systems 
 * due to security reasons, unless it is a concious decision and 
 * you are perfectly aware of security implications of accepting 
 * self-signed certificates
 * </p>
 *
 * <p>
 * Example of using custom protocol socket factory for a specific host:
 *     <pre>
 *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 *
 *     URI uri = new URI("https://localhost/", true);
 *     // use relative url only
 *     GetMethod httpget = new GetMethod(uri.getPathQuery());
 *     HostConfiguration hc = new HostConfiguration();
 *     hc.setHost(uri.getHost(), uri.getPort(), easyhttps);
 *     HttpClient client = new HttpClient();
 *     client.executeMethod(hc, httpget);
 *     </pre>
 * </p>
 * <p>
 * Example of using custom protocol socket factory per default instead of the standard one:
 *     <pre>
 *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 *     Protocol.registerProtocol("https", easyhttps);
 *
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https://localhost/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p>
 * 
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
 * 
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * for use without additional customization.
 * </p>
 */

public class EasySSLProtocolSocketFactory implements SecureProtocolSocketFactory {

    private static final String[] m_protocols = new String[] {
          "TLSv1.1", 
          "TLSv1.2" 
    };
    /** Log object for this class. */
    private static final Logger LOG = LogManager.getLogger(EasySSLProtocolSocketFactory.class);

    private SSLContext sslcontext = null;

    /**
     * Constructor for EasySSLProtocolSocketFactory.
     */
    public EasySSLProtocolSocketFactory() {
        super();
    }

    private static SSLContext createEasySSLContext() {
        try {
      
           SSLContext context = SSLContext.getInstance("TLS");
           
            context.init(
              null, 
              new TrustManager[] {new EasyX509TrustManager(null)}, 
              new java.security.SecureRandom());

            return context;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new HttpClientError(e.toString());
        }
    }

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {

       SSLSocket socket = (SSLSocket) getSSLContext().getSocketFactory().createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
      
      socket.setEnabledProtocols(m_protocols);
      socket.addHandshakeCompletedListener(new MyHandshakeListener());
      
      return socket;
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a 
     * controller thread is executed. The controller thread attempts to create a new socket 
     * within the given limit of time. If socket constructor does not return until the 
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext().getSocketFactory();
        SSLSocket socket = null;
        if (timeout == 0) {
            socket = (SSLSocket) socketfactory.createSocket(host, port, localAddress, localPort);
        } else {
            socket =  (SSLSocket)socketfactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            
        }
        socket.setEnabledProtocols(m_protocols);
        socket.addHandshakeCompletedListener(new MyHandshakeListener());
        
            return socket;
        }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
       SSLSocket socket = (SSLSocket) getSSLContext().getSocketFactory().createSocket(
            host,
            port
        );
       
       socket.setEnabledProtocols(m_protocols);
       socket.addHandshakeCompletedListener(new MyHandshakeListener());
       
       return socket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
        Socket socket1,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) getSSLContext().getSocketFactory().createSocket(
            socket1,
            host,
            port,
            autoClose
        );
        socket.setEnabledProtocols(m_protocols);
        socket.addHandshakeCompletedListener(new MyHandshakeListener());
        
        return socket;
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
    }

    public int hashCode() {
        return EasySSLProtocolSocketFactory.class.hashCode();
    }

    class MyHandshakeListener implements HandshakeCompletedListener {
       public void handshakeCompleted(HandshakeCompletedEvent e) {
          LOG.debug("Handshake succesful!");
          LOG.debug("Using cipher suite: " + e.getCipherSuite());
          LOG.debug("Using protocol : " + e.getSession().getProtocol());
       }
     }
    
}

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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.container;


import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IPSConnector {

    String SCHEME_HTTPS = "https";
    String SCHEME_HTTP = "http";
    String PROTOCOL_HTTP = "HTTP/1.1";
    String PROTOCOL_AJP = "AJP/1.3";


    String getScheme();

    void setScheme(String scheme);

    String getHostAddress();

    void setHostAddress(String getHostAddress);

    int getPort();

    void setPort(int port);

    Path getKeystoreFile();

    void setKeystoreFile(Path keystoreFile);

    String getKeystorePass();

    void setKeystorePass(String keystorePass);

    Path getTruststoreFile();

    void setTruststoreFile(Path truststoreFile);

    String getTruststorePass();

    void setTruststorePass(String truststorePass);

    Set<String> getCiphers();

    void setCiphers(Set<String> ciphers);

    Set<String> getSslProtocols();

    void setSslProtocols(Set<String> protocols);
    
    String getProtocol();
    void setProtocol(String protocol);


    void copyFrom(IPSConnector c);

    default boolean isHttps() {
        return this.getScheme()!=null && this.getScheme().equals(SCHEME_HTTPS);
    }

    default boolean isHttp() {
        return (this.getProtocol()!=null && this.getProtocol().equals(PROTOCOL_HTTP)) && Optional.ofNullable(this.getScheme()).orElse(SCHEME_HTTP).equals(SCHEME_HTTP);
    }

    default  boolean isAJP(){
        return (this.getProtocol()!=null && this.getProtocol().equals(PROTOCOL_AJP));
    }
    default String getCallbackHost() {
        return (this.getHostAddress()==null || this.getHostAddress().equals("0.0.0.0")) ? "127.0.0.1" : this.getHostAddress();
    }


    Map<String,String> getProperties();

}

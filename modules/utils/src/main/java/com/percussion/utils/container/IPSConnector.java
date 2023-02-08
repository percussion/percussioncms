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

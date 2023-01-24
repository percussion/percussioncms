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

package com.percussion.tls;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WrappedTrustManager  implements X509TrustManager {

    private LinkedHashMap<String,X509TrustManager> wrappedManagers = new LinkedHashMap<>();

    WrappedTrustManager() {
        addKeyStore("Default Java",null);
    }

    public void addKeyStore(String name,KeyStore keyStore)
    {
        try {
            wrappedManagers.put(name,getTrustManager(keyStore));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm",e);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Not adding keystore due to error",e);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        // If you're planning to use client-cert auth,
        // merge results from "defaultTm" and "myTm".
        Set<X509Certificate> accepted = new HashSet<>();
        for ( Map.Entry<String,X509TrustManager> thistm : wrappedManagers.entrySet())
        {
            accepted.addAll(Arrays.asList(thistm.getValue().getAcceptedIssuers()));
        }
        return accepted.toArray(new X509Certificate[accepted.size()]);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {

        CertificateException exception = null;
        Exception ex = null;
        for ( Map.Entry<String,X509TrustManager> thistm : wrappedManagers.entrySet())
        {
            String successTm = thistm.getKey();
            try {
                thistm.getValue().checkServerTrusted(chain, authType);
                System.out.println("Succss with "+successTm);
                return;
            } catch (CertificateException e)
            {
                System.out.println("Check failed for "+thistm.getKey());
                exception = e;
            }

        }
        System.out.println("Failed to validate Server certificate");

        throw exception;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {
        // If you're planning to use client-cert auth,
        // do the same as checking the server.
        CertificateException exception = null;
        for ( Map.Entry<String,X509TrustManager> thistm : wrappedManagers.entrySet()) {
            try {
                thistm.getValue().checkClientTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
                exception = e;
            }
        }
        throw exception;
    }

    private static X509TrustManager getTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
// Using null here initialises the TMF with the default trust store.
        tmf.init(keystore);

// Get hold of the default trust manager

        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }
}

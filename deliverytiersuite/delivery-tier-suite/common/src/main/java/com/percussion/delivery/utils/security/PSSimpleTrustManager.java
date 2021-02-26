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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A very simple trust manager for allowing an ssl connection.
 * 
 * @author erikserating
 * 
 */
public class PSSimpleTrustManager implements X509TrustManager
{
    private X509TrustManager standardTrustManager = null;

    /** Log object for this class. */
    private final Log LOG = LogFactory.getLog(PSSimpleTrustManager.class);

    /**
     * Constructor for EasyX509TrustManager.
     */
    public PSSimpleTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException
    {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0)
        {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],
     *      String authType)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        standardTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],
     *      String authType)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        if ((certificates != null) && LOG.isDebugEnabled())
        {
            LOG.debug("Server certificate chain:");
            for (int i = 0; i < certificates.length; i++)
            {
                LOG.debug("X509Certificate[" + i + "]=" + certificates[i]);
            }
        }
        if ((certificates != null) && (certificates.length == 1))
        {
            certificates[0].checkValidity();
        }
        else
        {
            standardTrustManager.checkServerTrusted(certificates, authType);
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}

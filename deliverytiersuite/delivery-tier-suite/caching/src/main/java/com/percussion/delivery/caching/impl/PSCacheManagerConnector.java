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
package com.percussion.delivery.caching.impl;

import com.percussion.delivery.caching.data.PSCacheConfig;
import com.percussion.delivery.caching.data.PSCacheRegion;
import com.percussion.delivery.caching.data.PSInvalidateRequest;
import com.percussion.delivery.caching.utils.PSJaxbUtils;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


/**
 * The cache manager connector manages flush request for the meta data indexer service.
 * It registers itself as a listener to know when data is changed so it can then process
 * a cache flush request.
 * @author erikserating
 *
 */
public class PSCacheManagerConnector implements IPSServiceDataChangeListener
{
    private String cacheManagerHost;
    private String username;
    private String password;
    private boolean passwordEncrypted;
    private int interRequestWait = 30;
    private int maxWait = 360;
    
    // Map tasks to cache regions. Hashtable because thread safe
    Hashtable<String, FlushTask> tasks = new Hashtable<String, FlushTask>();
    private final String PERC_CACHING_MANAGER_CONFIG = "/perc-caching/manager/config";
    Set<String> cacheRegions;
    
    public static Log log = LogFactory.getLog(PSCacheManagerConnector.class);
    
    public PSCacheManagerConnector()
    {
    }
    
    /* (non-Javadoc)
     * @see com.percussion.metadata.event.IPSMetadataListener#dataChanged()
     */
    public void dataChanged(Set<String> sites, String[] services)
    {
        log.debug("dataChanged event caught.");
        
        if(sites == null || sites.size() == 0)
            return;
        
        if(services == null || services.length == 0)
            return;
        
        if(this.shouldIgnoreEvent(sites, services))
            return;
        
        // iterate over sites and services
        for(String siteName : sites)
        {
            for(String service : services)
            {
                String cacheRegionName = getCacheRegionNameFromSiteAndServiceNames(siteName, service);
                FlushTask task = tasks.get(cacheRegionName);
                if(task == null || !task.isAlive())
                {
                    log.debug("Creating and starting new flush task.");
                    task = new FlushTask(cacheRegionName);
                    task.start();
                    tasks.put(cacheRegionName, task);
                }
                else
                {
                    if(task.isPaused())
                        task.restart();
                }
            }
        }
    }

    /**
     * Utility method to check if cache region has been configured.
     * If it has not been configured, then there is nothing to do
     * and event should be ignored. Caches configuration from
     * CacheManager and checks to see if the event's cacheRegions
     * are configured in the CacheManager. 
     * @param sites - set of sites for event
     * @param services - set of services for event
     * @return
     */
    private boolean shouldIgnoreEvent(Set<String> sites, String[] services)
    {
        try
        {
            if(this.cacheRegions == null)
                this.cacheRegions = getCacheRegionsFromCacheManager();
            if(this.cacheRegions == null)
                return true;
        }
        catch(Exception e)
        {
            log.error("Unable to load cache regions from cache manager", e);
            return true;
        }

        for(String siteName : sites)
        {
            for(String service : services)
            {
                String cacheName = getCacheRegionNameFromSiteAndServiceNames(siteName, service);
                if(this.cacheRegions.contains(cacheName))
                    return false;
            }
        }
        return true;
    }
    
    /**
     * Utility method to get the name of the cache region from the site and service name.
     * Right now the name is just the concatenation of the name of the site and the name of the service
     * @param siteName
     * @param serviceName
     * @return siteName_serviceName
     */
    private String getCacheRegionNameFromSiteAndServiceNames(String siteName, String serviceName)
    {
        return siteName + "_" + serviceName;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.metadata.event.IPSMetadataListener#dataChangeRequested()
     */
    public void dataChangeRequested(Set<String> sites, String[] services)
    {
       log.debug("dataChangeRequested event caught.");

       for(String siteName : sites)
       {
           for(String serviceName : services)
           {
               String cacheRegionName = getCacheRegionNameFromSiteAndServiceNames(siteName, serviceName);
               FlushTask task = tasks.get(cacheRegionName);
               if(task != null && task.isAlive()) 
               {
                   task.pause();
               }
           }
       }

    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the interRequestWait (in seconds)
     */
    public int getInterRequestWait()
    {
        return interRequestWait;
    }

    /**
     * @param interRequestWait the interRequestWait to set
     */
    public void setInterRequestWait(int interRequestWait)
    {
        this.interRequestWait = interRequestWait;
    }

    /**
     * @return the maxWait (in seconds)
     */
    public int getMaxWait()
    {
        return maxWait;
    }

    /**
     * @param maxWait the maxWait to set
     */
    public void setMaxWait(int maxWait)
    {
        this.maxWait = maxWait;
    }    
    
    /**
     * @return the cacheManagerHost
     */
    public String getCacheManagerHost()
    {
        return cacheManagerHost;
    }

    /**
     * @param cacheManagerHost the cacheManagerHost to set
     */
    public void setCacheManagerHost(String cacheManagerHost)
    {
        this.cacheManagerHost = cacheManagerHost;
    }

    /**
     * @return the passwordEncrypted
     */
    public boolean isPasswordEncrypted()
    {
        return passwordEncrypted;
    }

    /**
     * @param passwordEncrypted the passwordEncrypted to set
     */
    public void setPasswordEncrypted(boolean passwordEncrypted)
    {
        this.passwordEncrypted = passwordEncrypted;
    }

    /**
     * Retrieves the set of cache regions from the CacheManager configuration.
     * This is used to see if there is a configuration for the flush event for
     * a given cache region. Cache region names are the combination of the site
     * name and service name. These are configured in the CacheManager configuration
     * file. If there is no configuration for a given flush event, then the event
     * is ignored. This method loads the cache region configuration and is stored
     * locally to verify whether a flush event should ignored or not.
     * @return Set of cache regions
     * @throws Exception
     */
    private Set<String> getCacheRegionsFromCacheManager() throws Exception
    {

        Client client = getHttpClient();

        String url = cacheManagerHost + PERC_CACHING_MANAGER_CONFIG;
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_XML);
        Response response = invocationBuilder.get();

        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().build();
        webTarget.register(feature);
        invocationBuilder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username);
        invocationBuilder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);

        handleResponseStatusCode(response, "get the cache regions list");
        Object in = response.getEntity();
        PSCacheConfig cacheConfig = null;

        try {
            cacheConfig = PSJaxbUtils.unmarshall(in.toString(), PSCacheConfig.class, false);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }

        Set<PSCacheRegion> cacheRegions = new HashSet<PSCacheRegion>(cacheConfig.getCacheRegion());
        HashSet<String> cacheRegionNames = new HashSet<String>(cacheRegions.size());

        for(PSCacheRegion cacheRegion : cacheRegions)
        {
            String cacheRegionName = cacheRegion.getName();
            cacheRegionNames.add(cacheRegionName);
        }

        return cacheRegionNames;
    }

    /**
     * The flush task handles the sending of a flush request. It handles interRequestWait and
     * maxWait timers.
     * @author erikserating
     *
     */
    class FlushTask extends Thread {
        private boolean active = true;
        private boolean paused = false;
        private long maxRequestTime = -1;
        private long latestRequestTime = -1;
        private String cacheRegionName;

        public FlushTask(String cacheRegionName) {
            this.cacheRegionName = cacheRegionName;
            maxRequestTime = new Date().getTime() + (maxWait * 1000);
            resetTimer();
        }

        /**
         * Completely cancels the flush task. No flush request will occur if this is called.
         */
        public void cancel() {
            log.debug("cancelling flush request.");
            tasks.remove(this.cacheRegionName);
            active = false;
        }

        /**
         * Pauses the flush task to prevent a flush request except if we exceed the maxWait
         * time in which case a request will be sent regardless of pause state.
         */
        public void pause() {
            log.debug("pausing flush task");
            paused = true;
        }

        /**
         * Restarts the flush task resetting the retry time for the request
         * attempt.
         */
        public void restart() {
            log.debug("restarting flush task");
            resetTimer();
            paused = false;
        }

        /**
         * Resets the latestRequestTime which will cause the task to wait for
         * the interRequestWait period before attempting to send the flush request.
         */
        private void resetTimer() {
            latestRequestTime = new Date().getTime() + (interRequestWait * 1000);
        }

        /**
         * @return if <code>true</code> it indicates that the task is paused and will
         * wait until restart or exceeds maxWait before the flush request get attempted.
         */
        public boolean isPaused() {
            return paused;
        }

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            while (active) {
                long now = new Date().getTime();
                boolean maxExceeded = maxRequestTime <= now;
                boolean ready = latestRequestTime <= now;
                if ((!paused && ready) || maxExceeded) {
                    try {
                        doFlushRequest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    active = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }
            }
        }

        /**
         * Execute the flush request to the cache manager service.
         *
         * @throws Exception if any error occurs.
         */
        private void doFlushRequest() throws Exception {
            log.debug("Doing flush request");
            PSInvalidateRequest req = new PSInvalidateRequest();
            req.setRegionName(this.cacheRegionName);
            req.setType(PSInvalidateRequest.Type.REGION);
            String reqString = PSJaxbUtils.marshall(req, true);
            log.debug("Invalidate Request: "  + reqString);

            Client client = getHttpClient();

            WebTarget webTarget = client.target(cacheManagerHost + "/perc-caching/manager/invalidate");
            Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_XML);
            Response response = invocationBuilder.post(Entity.text(reqString));

            final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().build();
            webTarget.register(feature);
            invocationBuilder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username);
            invocationBuilder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);


            handleResponseStatusCode(response, "flush the cache");

            tasks.remove(this.cacheRegionName);
        }
        }

    private void handleResponseStatusCode(Response resp, String action) {
        int statusCode = resp.getStatus();

        if((statusCode / 100) != 2)
        {
            // error if we get a 5xx response (CML-4478)
            if((statusCode / 100) == 5)
            {
                log.error(
                        "Received the following error response while attempting to " + action  + ": " + resp.getStatus() + "(" + statusCode + ")");
            }
            else
            {
                // warn if we get a 1xx, 3xx, or 4xx response
                log.warn(
                        "Received the following response while attempting to " + action +": " + resp.getStatus() + "(" + statusCode + ")");
            }
        }
    }

        /**
         * Creates and returns an SSL enabled client.
         *
         * @return the client, never <code>null</code>.
         * @throws Exception if any error occurs.
         */
        private Client getHttpClient() throws Exception
        {
            log.debug("Creating ssl enabled client.");

            ClientConfig config = new ClientConfig();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new SimpleTrustManager(null)}, null);
            Client client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new HostnameVerifier()
                    {

                        public boolean verify(String s, SSLSession sslSession)
                        {
                            return true;
                        }
                    })
                    .withConfig(config)
                    .sslContext(ctx)
                    .build();
            return client;
        }

        /**
         * A very simple trust manager for allowing an ssl connection.
         *
         * @author erikserating
         */
        public class SimpleTrustManager implements X509TrustManager {
            private X509TrustManager standardTrustManager = null;

            /**
             * Log object for this class.
             */
            private final Log LOG = LogFactory.getLog(SimpleTrustManager.class);

            /**
             * Constructor for EasyX509TrustManager.
             */
            public SimpleTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init(keystore);
                TrustManager[] trustmanagers = factory.getTrustManagers();
                if (trustmanagers.length == 0) {
                    throw new NoSuchAlgorithmException("no trust manager found");
                }
                this.standardTrustManager = (X509TrustManager) trustmanagers[0];
            }

            /**
             * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
             */
            public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
                standardTrustManager.checkClientTrusted(certificates, authType);
            }

            /**
             * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String authType)
             */
            public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
                if ((certificates != null) && LOG.isDebugEnabled()) {
                    LOG.debug("Server certificate chain:");
                    for (int i = 0; i < certificates.length; i++) {
                        LOG.debug("X509Certificate[" + i + "]=" + certificates[i]);
                    }
                }
                if ((certificates != null) && (certificates.length == 1)) {
                    certificates[0].checkValidity();
                } else {
                    standardTrustManager.checkServerTrusted(certificates, authType);
                }
            }

            /**
             * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
             */
            public X509Certificate[] getAcceptedIssuers() {
                return this.standardTrustManager.getAcceptedIssuers();
            }
        }
    }


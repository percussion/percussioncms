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
package com.percussion.feeds.service.impl;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.security.PSEncryptor;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The feed info queue is a persistent queue that sends feed descriptors to the feed service in
 * the delivery tier. The queue processor runs in a separate thread.
 * @author erikserating
 *
 */
public class PSFeedsInfoQueue implements InitializingBean
{
    /**
     * The metadata service, initialized in the ctor, never <code>null</code>
     * after that.
     */
    private IPSMetadataService metadataService;

    /**
     * The delivery info service, initialized in the ctor, never <code>null</code>
     * after that.
     */
    private IPSDeliveryInfoService deliveryInfoService;

    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PSFeedsInfoQueue.class);

    @Autowired
    public PSFeedsInfoQueue(IPSMetadataService metadataService, IPSDeliveryInfoService deliveryInfoService)
    {
        this.metadataService = metadataService;
        this.deliveryInfoService = deliveryInfoService;
    }

    /**
     * Adds the descriptors for the specified site to the queue. Will overwrite any descriptors
     * that already exist for this site in the queue.
     * @param site the sitename, cannot be <code>null</code> or empty.
     * @param descriptors the descriptors json object string, cannot be <code>null</code>
     * or empty.
     */
    public void queueDescriptors(String site, String descriptors, String serverType) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException {
        if(StringUtils.isBlank(site))
            throw new IllegalArgumentException("site cannot be null or empty.");
        if(StringUtils.isBlank(descriptors))
            throw new IllegalArgumentException("descriptors cannot be null or empty.");
        if(serverType.equalsIgnoreCase("STAGING")){
            PSMetadata data = new PSMetadata(META_KEY_STAGING_PREFIX + site, descriptors);
            metadataService.save(data);
        }
        else
        {
            PSMetadata data = new PSMetadata(META_KEY_PREFIX + site, descriptors);
            metadataService.save(data);
        }

    }


    @Override
    public void afterPropertiesSet() throws Exception {

        QueueProcessor processor = new QueueProcessor();
        processor.start();
    }

    /**
     * Queue processor responsible for pulling items off the queue and sending
     * descriptors up the feed service. The queue will retry sending until all descriptors
     * are sent.
     * @author erikserating
     *
     */
    class QueueProcessor extends Thread implements PropertyChangeListener
    {

        public QueueProcessor(){
            super();

            //Register to get notified if encryption key changes
            PSEncryptor.getInstance("AES",
                    PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).addPropertyChangeListener(this);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {

            this.setName("PSFeedsInfoQueueRunner");
            PSDeliveryInfo prodService =  deliveryInfoService.findByService("perc-metadata-services","PRODUCTION");
            PSDeliveryInfo stagService =  deliveryInfoService.findByService("perc-metadata-services","STAGING");

            if(prodService == null)
            {
                log.error("No service entry found for: perc-metadata-services in delivery-servers.xml");
                return;
            }

            log.info("Starting feed info queue.");
            try
            {
                while (true)// Main process loop that never ends
                {
                    if (Thread.currentThread().isInterrupted())
                        break;

                    //Make sure secure key is up to date
                    checkKeyExchange();

                    Collection<PSMetadata> prodResults = metadataService.findByPrefix(META_KEY_PREFIX);
                    Collection<PSMetadata> stagResults = metadataService.findByPrefix(META_KEY_STAGING_PREFIX);

                    if (!prodResults.isEmpty())
                        if(checkForData(prodResults)){
                            sendDescriptors(prodService, prodResults);
                        }
                    if (!stagResults.isEmpty())
                        if(checkForData(stagResults)){
                            sendDescriptors(stagService, stagResults);
                        }


                    //Increased time - TODO: Re-architect this service
                    Thread.sleep(30000);
                }

            } catch (InterruptedException | IPSGenericDao.LoadException ignore){
                Thread.currentThread().interrupt();
            }
            finally
            {
                PSEncryptor.getInstance("AES",
                        PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                ).removePropertyChangeListener(this);
                log.info("Feed queue shutdown. interrupted="+Thread.currentThread().isInterrupted());
            }

        }

        /***
         * Validate that there are actually descriptors to publish.
         * @param prodResults
         * @return true if there are descriptors false if not.
         */
        private boolean checkForData(Collection<PSMetadata> prodResults) {

            for(PSMetadata p : prodResults){
                JSONArray json;
                try {
                    json = new JSONObject(p.getData()).getJSONArray("descriptors");
                } catch (JSONException e) {
                    log.error("Error parsing FeedDescriptors from Metadata store. Stopping Feed Publish",e);
                    return false;
                }
                if(json.length()>0)
                    return true;
            }
            return false;

        }

        private void sendDescriptors(PSDeliveryInfo deliveryInfo, Collection<PSMetadata> results ) throws InterruptedException
        {
            for (PSMetadata data : results)
            {
                String key = data.getKey();
                String val = data.getData();
                try
                {
                    JSONObject descriptors = new JSONObject(val);
                    //Add connection info
                    descriptors.put("serviceUrl", deliveryInfo.getUrl());
                    descriptors.put("serviceUser", deliveryInfo.getUsername());
                    descriptors.put("servicePass", deliveryInfo.getPassword());
                    descriptors.put("servicePassEncrypted", false);

                    String sitename = key.substring(META_KEY_STAGING_PREFIX.length());
                    boolean success = sendDescriptors(deliveryInfo, sitename, descriptors.toString());
                    if (success)
                    {
                        // dequeue entry
                        metadataService.delete(key);
                    }
                    Thread.sleep(1000); //Space out sends by 1 second
                }
                catch (InterruptedException e)
                {
                    throw e;
                }
                catch (Exception e)
                {

                    log.error("Feed service error", e);

                }
            }
        }

        /**
         * Sends descriptors to the feed service by using a put request.
         * @param serviceInfo assumed not <code>null</code>.
         * @param site assumed not <code>null</code> or empty.
         * @param descriptors assumed not <code>null</code> or empty.
         * @return <code>true</code> if successful.
         */
        private boolean sendDescriptors(PSDeliveryInfo serviceInfo, String site, String descriptors)
        {
            PSDeliveryInfo server = deliveryInfoService.findByService(PSDeliveryInfo.SERVICE_FEEDS, serviceInfo.getServerType(),serviceInfo.getAdminUrl());
            PSDeliveryClient deliveryClient = new PSDeliveryClient();

            try
            {
                Set<Integer> successfullHttpStatusCodes = new HashSet<>();
                successfullHttpStatusCodes.add(204);
                deliveryClient.push(
                        new PSDeliveryActionOptions()
                                .setActionUrl("/feeds/rss/descriptors")
                                .setDeliveryInfo(server)
                                .setHttpMethod(HttpMethodType.PUT)
                                .setSuccessfullHttpStatusCodes(successfullHttpStatusCodes )
                                .setAdminOperation(true),
                        descriptors);

                return true;

            }
            catch(Exception ex)
            {
                return false;
            }
        }

        /**
         * In the event that the DTS was down or a network error
         * happened when posting the current key, reprocess the event
         * so that we can be sure that the DTS servers have the current
         * key.
         */
        private void checkKeyExchange() {
            if (!keySuccessful && lastChangeEvent != null) {
                propertyChange(lastChangeEvent);
            }
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {


            //Event fired when the secure key used for encryption is changed
            if( evt != null && evt.getPropertyName().equalsIgnoreCase(PSEncryptor.SECRETKEY_PROPNAME)){
                    lastChangeEvent = evt;
                    List<PSDeliveryInfo> servers = deliveryInfoService.findAll();
                    List<String> processed = new ArrayList<>();
                    //There can be more than one DTS server.  Make sure we process each one.
                    boolean failed = false;
                    for(PSDeliveryInfo info : servers) {
                        if (info.getAvailableServices().contains(PSDeliveryInfo.SERVICE_FEEDS)) {
                            if (!processed.contains(info.getAdminUrl())) {

                                PSDeliveryClient deliveryClient = new PSDeliveryClient();

                                try {
                                    Set<Integer> successfullHttpStatusCodes = new HashSet<>();
                                    successfullHttpStatusCodes.add(204);
                                    deliveryClient.push(
                                            new PSDeliveryActionOptions()
                                                    .setActionUrl("/feeds/rotateKey")
                                                    .setDeliveryInfo(info)
                                                    .setHttpMethod(HttpMethodType.PUT)
                                                    .setSuccessfullHttpStatusCodes(successfullHttpStatusCodes)
                                                    .setAdminOperation(true),
                                            (byte[]) evt.getNewValue());
                                    processed.add(info.getAdminUrl());
                                    log.info("Updated security key pushed to DTS server: " + info.getAdminUrl());
                                } catch (Exception ex) {
                                    failed = true;
                                    log.warn("Unable to push updated security key to DTS server: " + info.getAdminUrl() + " Error was: " + ex.getMessage(), ex);
                                }
                            }
                        }
                    }
                    //If we had any failures lets flag the key to be reposted.
                    //TODO: Improve me. This would be more efficient if we only process the individual DTS instances that failed.
                    keySuccessful = !failed;
            }
        }

        /**
         * The last change event for the secureKey
         */
        private PropertyChangeEvent lastChangeEvent;

        /**
         * Flag to indicate if we have had a successful key exchange with the DTS
         */
        private boolean keySuccessful = false;
    }

    /**
     * Constant for the the feeds metadata service key prefix.
     */
    public static final String META_KEY_PREFIX = "PSFeedsInfoQueue.";

    public static final String META_KEY_STAGING_PREFIX = "Staging.";
}

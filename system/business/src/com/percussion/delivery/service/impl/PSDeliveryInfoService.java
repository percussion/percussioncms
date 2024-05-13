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

package com.percussion.delivery.service.impl;

import com.percussion.delivery.client.IPSDeliveryClient;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.delivery.service.PSDeliveryInfoServiceLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.PSEncryptor;
import com.percussion.server.PSServer;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author peterfrontiero
 *
 */
public class PSDeliveryInfoService implements IPSDeliveryInfoService
{

    private static final Logger log = LogManager.getLogger(PSDeliveryInfoService.class);

    public static final String DTS_CONFIG_FILENAME="rxconfig/DeliveryServer/delivery-servers.xml";

    /**
     * The configuration file path, never <code>null</code>.
     */
    private static final File CONFIG_FILE = new File(PSServer.getRxDir(),
            DTS_CONFIG_FILENAME);

    private boolean isConfigured=false;

    /**
     * @return the isConfigured
     */
    public boolean isConfigured()
    {
        return isConfigured;
    }

    /**
     * @param isConfigured the isConfigured to set
     */
    public void setConfigured(boolean isConfigured)
    {
        this.isConfigured = isConfigured;
    }

    /**
     * A list of delivery servers specified in the configure file.
     */
    private List<PSDeliveryInfo> servers = new ArrayList<>();

    public PSDeliveryInfoService() throws PSServerConfigException
    {
        if (!CONFIG_FILE.exists()){
            log.warn("DTS configuration file not found at :" + DTS_CONFIG_FILENAME + " skipping DTS configuration.");
            isConfigured = false;
            return;
        }


        PSDeliveryInfoLoader deliveryInfoLoader = new PSDeliveryInfoLoader(CONFIG_FILE);
        servers = deliveryInfoLoader.getDeliveryServers();
        isConfigured = true;
    }

    /*
     *  This constructor is for JUnit Testing purposes
     */
    public PSDeliveryInfoService(File file) throws PSServerConfigException
    {
        PSDeliveryInfoLoader deliveryInfoLoader = new PSDeliveryInfoLoader(file);
        servers = deliveryInfoLoader.getDeliveryServers();
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.service.IPSDeliveryService#findAll()
     */
    public List<PSDeliveryInfo> findAll()
    {
        return servers;
    }

    public List<String> getAdminUrls(String publishServerType){
        List<PSDeliveryInfo> allServers = findAll();

        List<String> serverList = new ArrayList<>();

        for (PSDeliveryInfo deliveryInfo : allServers) {
            if(publishServerType!=null) {
                if (deliveryInfo.getServerType() != null
                        && !deliveryInfo.getServerType().equalsIgnoreCase("license")
                        && deliveryInfo.getServerType().equalsIgnoreCase(publishServerType)) {
                    serverList.add(deliveryInfo.getAdminUrl());
                }
            }else{
                //If type is null we really shouldn't filter any.
                serverList.add(deliveryInfo.getAdminUrl());
            }
        }
        return serverList;
    }

    /**
     * find delivery server by server type
     * return first server found with type
     * if none found return null
     */
    public String findBaseByServerType(String type)
    {


        String baseUrl = "";
        if(StringUtils.isBlank(type))
        {
            type = PSPubServer.PRODUCTION;
        }
        PSDeliveryInfo inf = findServerByType(type);
        if(inf != null)
        {
            baseUrl = inf.getUrl();
        }
        return baseUrl;
    }

    /**
     *
     * @param adminUrlParam
     * @return
     */
    public String findBaseByServerName(String adminUrlParam)
    {
        String baseUrl = "";

        for (PSDeliveryInfo inf : servers)
        {
            String adminUrl = inf.getAdminUrl();
           //If the servertype is blank then treats it as PRODUCTION type
            if (adminUrl.equalsIgnoreCase(adminUrlParam))
            {
                baseUrl = inf.getUrl();
                break;
            }
        }
        return baseUrl;
    }

    private PSDeliveryInfo findServerByType(String type)
    {
        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = StringUtils.isBlank(inf.getServerType())?PSPubServer.PRODUCTION:inf.getServerType();
            //If the servertype is blank then treats it as PRODUCTION type
            if (dtype.equalsIgnoreCase(type))
            {
                delInfo = inf;
                break;
            }
        }
        return delInfo;
    }
    public static void copySecureKeyToDeliveryServer(IPSGuid edition) throws PSNotFoundException {
        String publishServer = null;
        boolean legacyEdition = false; //When true this is a legacy edition so skip this step
        if(edition != null) {
            IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
            if(pubService != null) {
                IPSEdition editionObject = pubService.loadEdition(edition);
                PSPubServer pubServer;
                if(editionObject != null && editionObject.getPubServerId()!=null) {
                     pubServer = PSPubServerDaoLocator.getPubServerManager()
                            .loadPubServer(editionObject.getPubServerId());
                    publishServer = pubServer.getPublishServer();
                }else{
                    legacyEdition = true;
                }
                
            }
        }
        //Skip dts processing for legacy editions.
        if(!legacyEdition) {
            PSDeliveryInfoService psDeliveryInfoService = (PSDeliveryInfoService) PSDeliveryInfoServiceLocator.getDeliveryInfoService();
            List<PSDeliveryInfo> psDeliveryInfoServiceList = psDeliveryInfoService.findAll();
            String secureKey = getSecureKey();
            if (secureKey == null) {
                return;
            }

            for (PSDeliveryInfo info : psDeliveryInfoServiceList) {
                //Copy only for passed in ServerID incase a serverId is passed.
                if (publishServer != null && !publishServer.equals(info.getAdminUrl())) {
                    continue;
                }
                if (info.getAvailableServices().contains(PSDeliveryInfo.SERVICE_FEEDS)) {
                    PSDeliveryClient deliveryClient = new PSDeliveryClient();
                    try {
                        Set<Integer> successfullHttpStatusCodes = new HashSet<>();
                        successfullHttpStatusCodes.add(204);
                        deliveryClient.push(
                                new IPSDeliveryClient.PSDeliveryActionOptions()
                                        .setActionUrl("/feeds/rss/rotateKey")
                                        .setDeliveryInfo(info)
                                        .setHttpMethod(IPSDeliveryClient.HttpMethodType.PUT)
                                        .setSuccessfullHttpStatusCodes(successfullHttpStatusCodes)
                                        .setAdminOperation(true),
                                secureKey);
                        log.info("Updated security key pushed to DTS server: {}", info.getAdminUrl());
                    } catch (Exception ex) {
                        log.warn("Unable to push updated security key to DTS server:{} ", info.getAdminUrl());
                        log.debug("Unable to push updated security key to DTS server:{}  ERROR: {} ", info.getAdminUrl(), ex.getMessage(), ex);
                    }
                }
            }
        }
    }

    private static String getSecureKey() {

        String keyLocation = PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR);
        String SECURE_KEY_FILE = ".key";
        String keyStr = null;
        Path secureKeyFile = Paths.get(keyLocation + SECURE_KEY_FILE);
        if (Files.exists(secureKeyFile)) {
            //load key
            try {
                byte[] key = Files.readAllBytes(secureKeyFile);
                keyStr = Base64.getEncoder().encodeToString(key);
            } catch (IOException e) {
                log.error("Error reading instance secure key file: {}", PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
        }
        return keyStr;
    }
    /* (non-Javadoc)
     * @see com.percussion.delivery.service.IPSDeliveryService#findByService()
     */
    public PSDeliveryInfo findByService(String service)
    {
        return findByService(service,null);
    }

    public PSDeliveryInfo findByService(String service, String type)
    {
        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = service.equalsIgnoreCase("perc-thirdparty-services")?
                    PSPubServer.LICENSE:PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = inf.getServerType();

            //If the servertype is blank then treats it as LICENSE if service is license service
            //else assume servertype PRODUCTION
            if(StringUtils.isBlank(dtype))
            {
                dtype = service.equalsIgnoreCase("perc-thirdparty-services")?
                        PSPubServer.LICENSE:PSPubServer.PRODUCTION;
            }

            if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type))
            {
                delInfo = inf;
                break;
            }
        }
        return delInfo;
    }



    public PSDeliveryInfo findByService(String service, String type,String adminURL){

        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = service.equalsIgnoreCase("perc-thirdparty-services")?
                    PSPubServer.LICENSE:PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = inf.getServerType();

            //If the servertype is blank then treats it as LICENSE if service is license service
            //else assume servertype PRODUCTION
            if(StringUtils.isBlank(dtype))
            {
                dtype = service.equalsIgnoreCase("perc-thirdparty-services")?
                        PSPubServer.LICENSE:PSPubServer.PRODUCTION;
            }

            if(adminURL!=null){
                if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type) && inf.getAdminUrl().equalsIgnoreCase(adminURL))
                {
                    delInfo = inf;
                    break;
                }
            }else{
                if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type))
                {
                    delInfo = inf;
                    break;
                }
            }

        }
        return delInfo;
    }



    public PSDeliveryInfo findByURL(String s)
            throws MalformedURLException
    {
        PSDeliveryInfo psdeliveryinfo = null;
        URL url = new URL(s);
        String[] as = url.getPath().split("/");
        if(as.length > 0)
            psdeliveryinfo = findByService(as[0]);
        return psdeliveryinfo;
    }

}

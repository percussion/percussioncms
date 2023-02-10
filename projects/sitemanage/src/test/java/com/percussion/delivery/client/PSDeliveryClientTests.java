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
package com.percussion.delivery.client;

import static java.util.Arrays.asList;

import java.util.ArrayList;

import com.percussion.utils.testing.IntegrationTest;
import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.util.Assert;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.impl.PSDeliveryInfoLoader;
import com.percussion.delivery.service.impl.PSDeliveryInfoLoaderTest;
import com.percussion.proxyconfig.data.PSProxyConfig;

/**
 * @author natechadwick
 * @author federicoromanelli
 *
 */
@Category(IntegrationTest.class)
public class PSDeliveryClientTests {
    
    private static final String NETSUITE_METHOD_URL = "/"
        + PSDeliveryInfo.SERVICE_THIRDPARTY + "/netsuite/method";
    
    private static final String PROXY_HOST_NO_AUTH = "10.10.10.70";
    private static final String PROXY_HOST_AUTH = "10.10.10.133";
    private static final String PROXY_HOST_INCORRECT = "10.10.10.155";
    PSDeliveryInfo info;
    PSDeliveryInfo info2;
    
    @Before
    public void setup(){
        PSDeliveryInfoLoaderTest loadUtil = new PSDeliveryInfoLoaderTest();
        
        PSDeliveryInfoLoader loader = loadUtil.getDeliveryInfoLoader("PercussionDeliveryServerConfigTest.xml");
        
        info = loader.getDeliveryServers().get(0);
        info2 = loader.getDeliveryServers().get(1);
        
        Assert.notNull(info);
        Assert.notNull(info2);
    }
    @Ignore
    @Test
    public void testSSLwithTLS(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSDeliveryActionOptions opt = new PSDeliveryActionOptions(info, "/perc-metadata-services/application.wadl", true);
        
        //try{
        c.push(opt,null);
    //  }catch(Exception e){
        //  System.out.print(e.getMessage());
    //  }
    }

    @Test
    public void testNoProxyConfig(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSProxyConfig proxyConfig = new PSProxyConfig();

        c.setProxyConfig(proxyConfig);
        c.setLicenseOverride("-1");
        JSONArray result = c.getJsonArray(
                new PSDeliveryActionOptions(info, NETSUITE_METHOD_URL,
                        HttpMethodType.GET, true));
        Assert.notNull(result);
    }
    
    @Test
    public void testNoProxyConfigBeanAvailable(){
        PSDeliveryClient c = new PSDeliveryClient();
        c.setLicenseOverride("-1");
        JSONArray result = c.getJsonArray(
                new PSDeliveryActionOptions(info, NETSUITE_METHOD_URL,
                        HttpMethodType.GET, true));
        Assert.notNull(result);
    }    
    
    @Test
    public void testProxyConfig(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSProxyConfig proxyConfig = new PSProxyConfig();
        proxyConfig.setHost(PROXY_HOST_NO_AUTH);
        proxyConfig.setPort("3128");
        proxyConfig.setProtocols(new ArrayList(asList("http", "https")));
        
        c.setProxyConfig(proxyConfig);
        c.setLicenseOverride("-1");
        JSONArray result = c.getJsonArray(
                new PSDeliveryActionOptions(info2, NETSUITE_METHOD_URL,
                        HttpMethodType.GET, true));
        Assert.notNull(result);

    }

    @Test
    public void testProxyConfigInvalidServer(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSProxyConfig proxyConfig = new PSProxyConfig();
        proxyConfig.setHost(PROXY_HOST_INCORRECT);
        proxyConfig.setPort("3128");
        proxyConfig.setProtocols(new ArrayList(asList("http", "https")));
        
        c.setProxyConfig(proxyConfig);
        c.setLicenseOverride("-1");
        try
        {
            JSONArray result = c.getJsonArray(
                    new PSDeliveryActionOptions(info, NETSUITE_METHOD_URL,
                            HttpMethodType.GET, true));
            // Shouldn't get to this point
            Assert.isTrue(false);
        }
        catch (Exception e)
        {
            Assert.isTrue(StringUtils.contains(e.getMessage(), "Unable to connect to delivery server"));
        }


    }
    
    @Test
    public void testProxyConfigUserAndPassword(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSProxyConfig proxyConfig = new PSProxyConfig();
        proxyConfig.setHost(PROXY_HOST_AUTH);
        proxyConfig.setPort("3128");
        proxyConfig.setUser("admin");
        proxyConfig.setPassword("demo");
        proxyConfig.setProtocols(new ArrayList(asList("http", "https")));
        
        c.setProxyConfig(proxyConfig);
        c.setLicenseOverride("-1");
        JSONArray result = c.getJsonArray(
                new PSDeliveryActionOptions(info, NETSUITE_METHOD_URL,
                        HttpMethodType.GET, true));
        Assert.notNull(result);
    }
    
    @Test
    public void testProxyConfigUserAndPasswordInvalidServer(){
        PSDeliveryClient c = new PSDeliveryClient();
        PSProxyConfig proxyConfig = new PSProxyConfig();
        proxyConfig.setHost(PROXY_HOST_AUTH);
        proxyConfig.setPort("3128");
        proxyConfig.setUser("admin");
        proxyConfig.setPassword("demo1");        
        proxyConfig.setProtocols(new ArrayList(asList("http", "https")));
        
        c.setProxyConfig(proxyConfig);
        c.setLicenseOverride("-1");
        try
        {
            JSONArray result = c.getJsonArray(
                    new PSDeliveryActionOptions(info, NETSUITE_METHOD_URL,
                            HttpMethodType.GET, true));
            // Shouldn't get to this point
            Assert.isTrue(false);
        }
        catch (Exception e)
        {
            Assert.isTrue(StringUtils.contains(e.getMessage(), "Unable to connect to delivery server"));
        }


    }    
}

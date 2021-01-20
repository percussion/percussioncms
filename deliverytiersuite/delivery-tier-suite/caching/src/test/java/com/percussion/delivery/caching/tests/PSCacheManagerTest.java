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
package com.percussion.delivery.caching.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.delivery.caching.IPSCacheProviderPlugin;
import com.percussion.delivery.caching.data.PSCacheConfig;
import com.percussion.delivery.caching.data.PSCacheProvider;
import com.percussion.delivery.caching.data.PSCacheRegion;
import com.percussion.delivery.caching.data.PSInvalidateRequest;
import com.percussion.delivery.caching.impl.PSCacheManager;
import com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin7;
import com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin8;
import com.percussion.delivery.caching.tests.fakeProviders.PSProviderPluginMock;

import java.util.Map;

import org.junit.Test;

public class
PSCacheManagerTest
{

    @Test
    public void testCacheManager_LoadConfiguration() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig1.xml"));
        cacheManager.init();
        
        PSCacheConfig PSCacheConfig = cacheManager.getConfig();
        
        assertNotNull("cache config not null", PSCacheConfig);
        assertEquals("cache region count", 2, PSCacheConfig.getCacheRegion().size());
        assertEquals("max queue workers - default", 5, PSCacheConfig.getMaxQueueWorkers());
        
        for (PSCacheRegion PSCacheRegion : PSCacheConfig.getCacheRegion())
        {
            assertNotNull("cache region's name not null", PSCacheRegion.getName());
            assertNotNull("provider not null", PSCacheRegion.getProvider());
            
            PSCacheProvider cacheProvider = PSCacheRegion.getProvider();
            
            if (PSCacheRegion.getName().equals("www.abc.com"))
            {
                assertEquals("provider name - 1",
                        "com.percussion.delivery.caching.plugins.akamai.PSAkamaiPlugin",
                        cacheProvider.getPlugin());
                
                // Provider properties
                Map<String, String> providerProperties = cacheProvider.getProperties();
                
                assertEquals("provider properties count - 1", 3, providerProperties.size());
                
                assertTrue("provider properties - has user", providerProperties.containsKey("user"));
                assertEquals("provider properties - user value", "foo", providerProperties.get("user"));
                
                assertTrue("provider properties - has password", providerProperties.containsKey("password"));
                assertEquals("provider properties - password value", "blah", providerProperties.get("password"));
                
                assertTrue("provider properties - has cp", providerProperties.containsKey("cp"));
                assertEquals("provider properties - cp value", "78999", providerProperties.get("cp"));
                
                // Web properties
                assertEquals("cache region's web properties count - 1", 2, PSCacheRegion.getWebProperties().size());
                
            }
            else if (PSCacheRegion.getName().equals("www.abcde.com"))
            {
                assertEquals("provider name - 1",
                        "com.percussion.delivery.caching.plugins.akamai.PSAnotherPlugin",
                        cacheProvider.getPlugin());
                
                // Provider properties
                Map<String, String> providerProperties = cacheProvider.getProperties();
                
                assertEquals("provider properties count - 1", 2, providerProperties.size());
                
                assertTrue("provider properties - has user", providerProperties.containsKey("user"));
                assertEquals("provider properties - user value", "foo2", providerProperties.get("user"));
                
                assertTrue("provider properties - has password", providerProperties.containsKey("password"));
                assertEquals("provider properties - password value", "blah2", providerProperties.get("password"));
                
                // Web properties
                assertEquals("cache region's web properties count - 1", 3, PSCacheRegion.getWebProperties().size());
                
            }
            else
                fail("Invalid cache region name");
        }
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPluginsEmpty() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_pluginsEmpty.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPluginClassDoesNotExist() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerClassDoesNotExist.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPluginClass_DoesNotImplementInterface() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerClassDoesNotExist.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPluginClass_ConstructorNotAccessible() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerClass_constructorNotAccessible.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPluginClass_IsAbstractClass() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerClass_isAbstractClass.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPlugin_InstantiationFailed() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerPlugin_instantiationFailed.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPlugin_InitializationFailed() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerPlugin_initializationFailed.xml"));
        cacheManager.init();
        
        assertNotNull("provider map not null", cacheManager.getProvidersMap());
        assertEquals("providers map - count", 0, cacheManager.getProvidersMap().size());
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPlugin_OnePlugin_SuccessfullyLoaded() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerPlugin_success1.xml"));
        cacheManager.init();
        
        Map<String, IPSCacheProviderPlugin> providersMap =
            cacheManager.getProvidersMap();
        
        String providerClass = "com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin7";
        
        assertNotNull("provider map not null", providersMap);
        assertEquals("providers map - count", 1, providersMap.size());
        assertTrue("providers map - contains plugin", providersMap.containsKey(providerClass));
        assertTrue("providers map - correct type", providersMap.get(providerClass) instanceof PSFakeProviderPlugin7);
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPlugin_TwoPlugins_SuccessfullyLoaded() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerPlugin_success2.xml"));
        cacheManager.init();
        
        Map<String, IPSCacheProviderPlugin> providersMap =
            cacheManager.getProvidersMap();
        
        String providerClass1 = "com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin7";
        String providerClass2 = "com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin8";
        
        assertNotNull("provider map not null", providersMap);
        assertEquals("providers map - count", 2, providersMap.size());
        
        assertTrue("providers map - contains plugin 1", providersMap.containsKey(providerClass1));
        assertTrue("providers map - correct type 1", providersMap.get(providerClass1) instanceof PSFakeProviderPlugin7);
        
        assertTrue("providers map - contains plugin 2", providersMap.containsKey(providerClass2));
        assertTrue("providers map - correct type 2", providersMap.get(providerClass2) instanceof PSFakeProviderPlugin8);
    }
    
    @Test
    public void testCacheManager_Initialization_ProviderPlugin_TwoSamePlugins_SuccessfullyLoaded() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_providerPlugin_success3.xml"));
        cacheManager.init();
        
        Map<String, IPSCacheProviderPlugin> providersMap =
            cacheManager.getProvidersMap();
        
        String providerClass = "com.percussion.delivery.caching.tests.fakeProviders.PSFakeProviderPlugin8";
        
        assertNotNull("provider map not null", providersMap);
        assertEquals("providers map - count", 1, providersMap.size());
        assertTrue("providers map - contains plugin", providersMap.containsKey(providerClass));
        assertTrue("providers map - correct type", providersMap.get(providerClass) instanceof PSFakeProviderPlugin8);
    }
    
    @Test
    public void testCacheManager_Invalidate_RegionDoesNotExist() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream(
                "CacheConfig_invalidateMethod1.xml"));
        cacheManager.init();

        PSInvalidateRequest invalidateRequest = new PSInvalidateRequest();
        invalidateRequest.setRegionName("www.invalid.com");

        cacheManager.invalidate(invalidateRequest);
        
        // There should be no calls to the provider
        String providerClass = "com.percussion.delivery.caching.tests.fakeProviders.PSProviderPluginMock";

        PSProviderPluginMock providerInstance =
            (PSProviderPluginMock) cacheManager.getProvidersMap().get(providerClass);
        
        assertEquals("provider - invalidate calls", 0, providerInstance.getInvalidateCalls());
    }
    
    @Test
    public void testCacheManager_Invalidate_Successful() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream(
                "CacheConfig_invalidateMethod1.xml"));
        cacheManager.init();

        // Get provider plugin instances
        String providerClass1 = "com.percussion.delivery.caching.tests.fakeProviders.PSProviderPluginMock";

        PSProviderPluginMock providerInstance1 =
            (PSProviderPluginMock) cacheManager.getProvidersMap().get(providerClass1);
        
        String providerClass2 = "com.percussion.delivery.caching.tests.fakeProviders.PSProviderPluginMock2";

        PSProviderPluginMock providerInstance2 =
            (PSProviderPluginMock) cacheManager.getProvidersMap().get(providerClass2);
        
        // Test first provider plugin
        PSInvalidateRequest invalidateRequest = new PSInvalidateRequest();
        invalidateRequest.setRegionName("www.abc.com");

        cacheManager.invalidate(invalidateRequest);
        
        assertEquals("provider - invalidate calls", 1, providerInstance1.getInvalidateCalls());
        assertEquals("provider - invalidate calls", 0, providerInstance2.getInvalidateCalls());
        
        // Test second provider plugin
        invalidateRequest = new PSInvalidateRequest();
        invalidateRequest.setRegionName("www.abc2.com");

        cacheManager.invalidate(invalidateRequest);
        
        assertEquals("provider - invalidate calls", 1, providerInstance1.getInvalidateCalls());
        assertEquals("provider - invalidate calls", 1, providerInstance2.getInvalidateCalls());
    }
    
    @Test
    public void testCacheManager_InvalidateFails() throws Exception
    {
        PSCacheManager cacheManager = new PSCacheManager(getClass().getResourceAsStream("CacheConfig_invalidateMethod2.xml"));
        cacheManager.init();
        
        PSInvalidateRequest invalidateRequest = new PSInvalidateRequest();
        invalidateRequest.setRegionName("www.abc2.com");
        
        // Cache manager should handle the exception
        cacheManager.invalidate(invalidateRequest);
    }
}

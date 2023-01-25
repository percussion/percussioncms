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

package com.percussion.sitemanage.service.impl;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;

import java.util.Collection;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;

import junit.framework.TestCase;

public class PSPublishStagingTest extends TestCase {

    public class TestablePSPublishStagingService extends
	    PSPublishStagingService {

	public TestablePSPublishStagingService(IPSMetadataService metadata) {
	    super(metadata);
	    // TODO Auto-generated constructor stub
	}

	private boolean stagingEnabled = false;
	@Override
	public boolean isStagingFeatureEnabled() {
	    return stagingEnabled;
	}

	/**
	 * @param stagingEnabled
	 *            the stagingEnabled to set
	 */
	public void setStagingFeatureEnabled(boolean stagingEnabled) {
	    this.stagingEnabled = stagingEnabled;
	}
    }

    public class MockMetadataService implements IPSMetadataService {

	private HashMap<String, PSMetadata> metadata = new HashMap<String, PSMetadata>();

	@Override
	public PSMetadata find(String key) {
	    return metadata.get(key);
	}

	@Override
	public Collection<PSMetadata> findByPrefix(String prefix) {
	    // Not implemented
	    return null;
	}

	@Override
	public void save(PSMetadata data) {
	    metadata.put(data.getKey(), data);
	}

	@Override
	public void delete(String key) {
	    metadata.remove(key);
	}

	@Override
	public void deleteByPrefix(String prefix) {
	    // NOT Implemented

	}

    }

    @Test
    public void testStagingActive() {
	TestablePSPublishStagingService stgService = new TestablePSPublishStagingService(new MockMetadataService());

	// false,false
	assertTrue(stgService.isStagingActive() == false);
	stgService.setStagingOn();
	// false,true
	assertTrue(stgService.isStagingActive() == false);
	stgService.setStagingFeatureEnabled(true);
	// true,true
	assertTrue(stgService.isStagingActive() == true);
	stgService.setStagingOff();
	// true, false
	assertTrue(stgService.isStagingActive() == false);
    }

}

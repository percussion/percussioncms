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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

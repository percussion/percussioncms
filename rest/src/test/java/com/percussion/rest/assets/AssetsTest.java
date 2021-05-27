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

package com.percussion.rest.assets;

import com.percussion.rest.MainTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertTrue;

@Category(IntegrationTest.class)
public class AssetsTest extends MainTest
{

    private static final Logger log = LogManager.getLogger(AssetsTest.class);
    
    @Test
    public void testRenameAsset(){
    	
        Entity<String> assetEntity = Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE);

        try {
            Asset response = target("assets/rename/Assets/path1/pathsub/pathsub2/page1.png/newname.png")
                    .request().post(assetEntity, Asset.class);
            assertTrue("New Name Should Match", response.getName().equals("newname.png"));
        } catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw e;
        }

    }

}

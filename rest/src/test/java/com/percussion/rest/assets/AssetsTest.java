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

package com.percussion.rest.assets;

import com.percussion.error.PSExceptionUtils;
import com.percussion.rest.MainTest;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw e;
        }

    }

}

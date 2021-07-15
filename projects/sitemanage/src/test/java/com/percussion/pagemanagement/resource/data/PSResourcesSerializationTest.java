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
package com.percussion.pagemanagement.resource.data;

import static java.util.Arrays.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFolderResource;
import com.percussion.share.dao.PSSerializerUtils;

public class PSResourcesSerializationTest
{
    
    @Test
    public void testXmlResources() throws Exception {
         PSResourceDefinitionGroup rs = new PSResourceDefinitionGroup();
         PSFileResource fr = new PSFileResource();
         PSAssetResource ar = new PSAssetResource();
         PSFolderResource f = new PSFolderResource();
         ar.setId("ben");
         ar.setLegacyTemplate("percBinary");
         ar.setContentType("percBinary");
         fr.setId("adam");
         fr.setType(PSResourceDefinitionGroup.PSFileResource.PSFileResourceType.css);
         fr.setFile("/my/file.css");
         f.setId("peter");
         f.setPath("/Stuff/My");
         rs.setFileResources(asList(fr));
         rs.setAssetResources(asList(ar));
         rs.setFolderResources(asList(f));
         String actual = PSSerializerUtils.marshal(rs);
         log.debug(actual);
         
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSResourcesSerializationTest.class);

}

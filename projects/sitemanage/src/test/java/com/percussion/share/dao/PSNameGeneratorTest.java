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
package com.percussion.share.dao;

import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSNameGeneratorTest extends ServletTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    public void testGetLocalContentName() throws Exception
    {
        String name1 = nameGenerator.generateLocalContentName();
        assertTrue(!StringUtils.isBlank(name1));
        String name2 = nameGenerator.generateLocalContentName();
        assertTrue(!StringUtils.isBlank(name2));
        assertTrue(!name2.equals(name1));
    }
    
    public IPSNameGenerator getNameGenerator()
    {
        return nameGenerator;
    }

    public void setNameGenerator(IPSNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }
    
    private IPSNameGenerator nameGenerator;
      
}

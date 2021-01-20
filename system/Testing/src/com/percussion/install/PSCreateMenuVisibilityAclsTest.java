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
package com.percussion.install;

import com.percussion.testing.PSAbstractSpringContextTest;
import com.percussion.utils.annotations.IgnoreInWebAppSpringContext;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.SpringContextTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test menu visibility acl install plugin
 * 
 * @author dougrand
 */
@Category({IntegrationTest.class, SpringContextTest.class})
@RunWith(SpringJUnit4ClassRunner.class)
@IgnoreInWebAppSpringContext
public class PSCreateMenuVisibilityAclsTest extends PSAbstractSpringContextTest
{
   /**
    * Test visibility acl checker
    */
   @Test
   public void testMenuVisibility() 
   {
      PSCreateMenuVisibilityAcls mva = new PSCreateMenuVisibilityAcls();
      
      mva.process(null, null);
   }
}

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
package com.percussion.rx.config.test;

import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.services.pkginfo.utils.PSPkgHelper;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for config service tests which apply configurations.  Disables
 * package element version updates on setup and re-enables updates on teardown.
 */
@Category(IntegrationTest.class)
public abstract class PSConfigurationTest extends ServletTestCase
{
   @Override
   protected void setUp() throws Exception
   { 
      // disable package element updates
      PSPkgHelper.setEnabled(false);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      // re-enable package element updates
      PSPkgHelper.setEnabled(true);
   }
   
   /**
    * Gets the configure handler which used to process the given setter.
    * 
    * @param setter the setter, assumed not <code>null</code>.
    * 
    * @return the handler, never <code>null</code>. 
    */
   public PSObjectConfigHandler getConfigHandler(IPSPropertySetter setter)
   {
      List<IPSPropertySetter> ss = new ArrayList<IPSPropertySetter>();
      ss.add(setter);
      PSObjectConfigHandler h = new PSObjectConfigHandler();
      h.setPropertySetters(ss);

      return h;
   }
}

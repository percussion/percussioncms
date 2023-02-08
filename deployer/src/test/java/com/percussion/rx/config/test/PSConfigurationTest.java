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

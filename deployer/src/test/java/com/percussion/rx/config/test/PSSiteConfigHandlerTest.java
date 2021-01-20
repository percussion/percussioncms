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

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.impl.PSConfigMapper;
import com.percussion.rx.config.impl.PSSiteConfigHandler;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for testing the properties of {@link PSSiteConfigHandler}.
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSSiteConfigHandlerTest
{
   @Test
   public void testConfigHandler() throws Exception
   {
      String prefix = "com.percussion.RSS.";
      String NAMES_KEY = prefix + "names";

      // test CURRENT only for the "names" property
      Map<String, Object> curProps = new HashMap<String, Object>();
      curProps.put(NAMES_KEY, Collections.singletonList(CI_SITE));
      Map<String, Object> prevProps = new HashMap<String, Object>();

      processAndValidate(ObjectState.CURRENT, null, curProps, prevProps);

      // test BOTH only for the "names" property
      prevProps.put(NAMES_KEY, Collections.singletonList(CI_SITE));

      processAndValidate(ObjectState.BOTH, null, curProps, prevProps);

      // test CURRENT & PREVIOUS for the "names" property
      curProps.clear();
      curProps.put(NAMES_KEY, Collections.singletonList(CI_SITE));
      prevProps.clear();
      prevProps.put(NAMES_KEY, Collections.singletonList(EI_SITE));

      processAndValidate(ObjectState.CURRENT, ObjectState.PREVIOUS, curProps,
            prevProps);

      // test BOTH & CURRENT for the "names" property
      curProps.clear();
      List<String> allSites = new ArrayList<String>();
      allSites.add(CI_SITE);
      allSites.add(EI_SITE);
      curProps.put(NAMES_KEY, allSites);
      prevProps.clear();
      prevProps.put(NAMES_KEY, Collections.singletonList(CI_SITE));

      processAndValidate(ObjectState.BOTH, ObjectState.CURRENT, curProps,
            prevProps);

      // test BOTH & PREVIOUS for the "names" property
      curProps.clear();
      curProps.put(NAMES_KEY, Collections.singletonList(CI_SITE));
      allSites.clear();
      prevProps.clear();
      allSites.add(CI_SITE);
      allSites.add(EI_SITE);
      prevProps.put(NAMES_KEY, allSites);

      processAndValidate(ObjectState.BOTH, ObjectState.PREVIOUS, curProps,
            prevProps);
   }

   /**
    * Process and validate the processed result.
    * 
    * @param ciState the state of {@link #CI_SITE} site, never <code>null</code>.
    * @param eiState the state of {@link #EI_SITE} site, may be
    * <code>null</code>.
    * @param curProps the current properties, never <code>null</code>.
    * @param prevProps the previous properties, never <code>null</code>.
    */
   private void processAndValidate(ObjectState ciState, ObjectState eiState,
         Map<String, Object> curProps, Map<String, Object> prevProps) throws IOException {
      PSConfigMapper mapper = new PSConfigMapper();
      File def = PSResourceUtils.getFile(PSSiteConfigHandlerTest.class,CONFIG_DEF,null);

      List<IPSConfigHandler> handlers = mapper.getResolvedHandlers(def.getAbsolutePath(),
            curProps, curProps, prevProps);

      IPSConfigHandler handler = handlers.get(0);
      List<PSPair<String, ObjectState>> sites = handler.getObjectNames();

      if (eiState == null)
         assertTrue(sites.size() == 1);
      else
         assertTrue(sites.size() == 2);
      for (PSPair<String, ObjectState> site : sites)
      {
         if (site.getFirst().equals(CI_SITE))
            assertTrue(site.getSecond().equals(ciState));
         else
            assertTrue(site.getSecond().equals(eiState));
      }
   }

   private static String CI_SITE = "Corporation_Investment";

   private static String EI_SITE = "Enterprise_Investment";

   public static final String PKG_NAME = "PSSiteConfigHandlerTest";

   public static final String CONFIG_DEF = "/com/percussion/rx/config/test/"
         + PKG_NAME + "_configDef.xml";

}

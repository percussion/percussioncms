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
 * Tests {@link PSLocationSchemeConfigHandler}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSLocationSchemeConfigHandlerTest
{
   @Test
   public void testConfigHandler() throws Exception
   {
      String prefix = "com.percussion.RSS.";
      String CTXS_KEY = prefix + "contexts";

      // test CURRENT only for the "names" property
      Map<String, Object> curProps = new HashMap<String, Object>();
      curProps.put(CTXS_KEY, Collections.singletonList(PUBLISH));
      Map<String, Object> prevProps = new HashMap<String, Object>();

      processAndValidate(ObjectState.CURRENT, null, curProps, prevProps);

      // test BOTH only for the "contexts" property
      prevProps.put(CTXS_KEY, Collections.singletonList(PUBLISH));

      processAndValidate(ObjectState.BOTH, null, curProps, prevProps);

      // test CURRENT & PREVIOUS for the "contexts" property
      curProps.clear();
      curProps.put(CTXS_KEY, Collections.singletonList(PUBLISH));
      prevProps.clear();
      prevProps.put(CTXS_KEY, Collections.singletonList(ASSEMBLY));

      processAndValidate(ObjectState.CURRENT, ObjectState.PREVIOUS, curProps,
            prevProps);

      // test BOTH & CURRENT for the "contexts" property
      curProps.clear();
      List<String> allSites = new ArrayList<String>();
      allSites.add(PUBLISH);
      allSites.add(ASSEMBLY);
      curProps.put(CTXS_KEY, allSites);
      prevProps.clear();
      prevProps.put(CTXS_KEY, Collections.singletonList(PUBLISH));

      processAndValidate(ObjectState.BOTH, ObjectState.CURRENT, curProps,
            prevProps);

      // test BOTH & PREVIOUS for the "contexts" property
      curProps.clear();
      curProps.put(CTXS_KEY, Collections.singletonList(PUBLISH));
      allSites.clear();
      prevProps.clear();
      allSites.add(PUBLISH);
      allSites.add(ASSEMBLY);
      prevProps.put(CTXS_KEY, allSites);

      processAndValidate(ObjectState.BOTH, ObjectState.PREVIOUS, curProps,
            prevProps);
      
   }

   /**
    * Process and validate the processed result.
    * 
    * @param pubState the state of Location Scheme in {@link #PUBLISH} context,
    * never <code>null</code>.
    * @param assemblyState the state of Location Scheme in {@link #ASSEMBLY}
    * context, may be <code>null</code>.
    * @param curProps the current properties, never <code>null</code>.
    * @param prevProps the previous properties, never <code>null</code>.
    */
   private void processAndValidate(ObjectState pubState,
         ObjectState assemblyState, Map<String, Object> curProps,
         Map<String, Object> prevProps) throws IOException {
      PSConfigMapper mapper = new PSConfigMapper();
      File def = PSResourceUtils.getFile(PSLocationSchemeConfigHandlerTest.class,CONFIG_DEF,null);

      List<IPSConfigHandler> handlers = mapper.getResolvedHandlers(def.getAbsolutePath(),
            curProps, curProps, prevProps);

      IPSConfigHandler handler = handlers.get(0);
      List<PSPair<String, ObjectState>> schemes = handler.getObjectNames();

      if (assemblyState == null)
         assertTrue(schemes.size() == 1);
      else
         assertTrue(schemes.size() == 2);
      for (PSPair<String, ObjectState> scheme : schemes)
      {
         if (scheme.getFirst().startsWith(PUBLISH))
            assertTrue(scheme.getSecond().equals(pubState));
         else
            assertTrue(scheme.getSecond().equals(assemblyState));
      }
   }

   private static String PUBLISH = "Publish";

   private static String ASSEMBLY = "Site_Folder_Assembly";

   public static final String PKG_NAME = "PSLocationSchemeConfigHandlerTest";

   public static final String CONFIG_DEF = "/com/percussion/config/"
         + PKG_NAME + "_configDef.xml";

}

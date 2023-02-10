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
package com.percussion.rxfix.dbfixes;

import com.percussion.install.RxUpgrade;
import com.percussion.rxfix.IPSFix;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.InputStream;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.w3c.dom.Document;

/**
 * This fix removes relationship slots that are no longer referenced. Any given
 * slot should be accessible either as an inline slot, these are constant across
 * content types, or a variant slot, which are specific to a particular content
 * type's variant.
 * 
 * <p>
 * The algorithm is to make a list of the inline slots, and then iterate over
 * all content types. For each content type, we do a query that returns the list
 * of slots. The two lists are then unioned into a new list, and that list is
 * used to find relationship records that are orphaned.
 */
public class PSFixOrphanedData extends PSFixDBBase implements IPSFix
{
   /**
    * Ctor
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixOrphanedData() throws NamingException, SQLException {
      super();
   }

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);

      RxUpgrade upgrader = new RxUpgrade();

      try {
         Class clazz = getClass();
         try (InputStream is = clazz
                 .getResourceAsStream("rxOrphanedDataCleanupPlugins.xml")) {

            if (is == null) {
               logWarn(null,
                       "Skipping orphaned data cleanup as plugin file is missing");
            } else if (!preview) {
               Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);

               upgrader.process(PSServer.getRxDir().getAbsolutePath(), doc);
            } else {
               logPreview(null, "Skipping orphaned data cleanup in preview mode");
            }
         }
      }
      catch (Exception ex)
      {
         logFailure(null, ex.getLocalizedMessage());
      }
   }
   
   @Override
   public String getOperation()
   {
      return "Fix orphaned data";
   }
}

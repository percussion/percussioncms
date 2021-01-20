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

      try
      {
         Class clazz = getClass();
         InputStream is = clazz
               .getResourceAsStream("rxOrphanedDataCleanupPlugins.xml");

         if (is == null)
         {
            logWarn(null,
                  "Skipping orphaned data cleanup as plugin file is missing");
         }
         else if (!preview)
         {
            Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);

            upgrader.process(PSServer.getRxDir().getAbsolutePath(), doc);
         }
         else
         {
            logPreview(null, "Skipping orphaned data cleanup in preview mode");
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
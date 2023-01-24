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
package com.percussion.services.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.services.contentmgr.data.PSRowComparator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSContentListGenerator;
import com.percussion.utils.jsr170.PSLongValue;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base generator class, used for more than one implementation.
 * 
 * @author dougrand
 */
public abstract class PSBaseGenerator implements IPSContentListGenerator
{
   /**
    * The logger
    */
   protected static Logger ms_log = LogManager.getLogger(PSBaseGenerator.class);

   /**
    * The cms service
    */
   protected static IPSCmsObjectMgr ms_cms = PSCmsObjectMgrLocator
         .getObjectManager();

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Create an empty query result with the contentId and folderId columns
    * 
    * @return The empty query result to which rows can be added, never <code>null</code>.
    */
   protected PSQueryResult createQueryResult()
   {
      List<PSPair<String, Boolean>> fields = new ArrayList<>();
      fields.add(new PSPair<>(
            IPSContentPropertyConstants.RX_SYS_CONTENTID, true));
      PSRowComparator comparator = new PSRowComparator(fields);
      String[] columns =
      {IPSContentPropertyConstants.RX_SYS_CONTENTID,
            IPSContentPropertyConstants.RX_SYS_FOLDERID};
      PSQueryResult qr = new PSQueryResult(columns, comparator);
      return qr;
   }

   /**
    * Add a single content id to the result set
    * 
    * @param qr the results, assumed never <code>null</code>
    * @param contentid the id
    */
   protected void addToResults(PSQueryResult qr, int contentid)
   {
      addToResults(qr, contentid, -1);
   }
   
   protected void addToResults(PSQueryResult qr, int contentid, int folderid)
   {
      Map<String, Object> data = new HashMap<>();
      Value idval = new PSLongValue(contentid);
      data.put(IPSContentPropertyConstants.RX_SYS_CONTENTID, idval);
      if (folderid != -1)
      {
         Value folderval = new PSLongValue(folderid);
         data.put(IPSContentPropertyConstants.RX_SYS_FOLDERID, folderval);         
      }
      PSRow row = new PSRow(data);
      qr.addRow(row);
   }
}

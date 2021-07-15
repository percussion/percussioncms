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
package com.percussion.install;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSExtensionInstallTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Element;


/**
 * This upgrade plug-in will add/modify extensions that have these interfaces
 * included:
 * IPSResultDocumentProcessor    (may include) --> IPSItemValidator
 * IPSUdfProcessor               (may include) --> IPSFieldValidator
 * IPSUdfProcessor               (may include) --> IPSFieldOutputTransformer
 * 
 * A list of all the extension names that need these modifications is pre-
 * computed. So in future if you need to add/delete/modify these lists do so 
 * here and run this tool standalone on the source extensions.xml
 * These modifications are performed only on JAVA handlers that are not 
 * deprecated and 
 */
public class PSUpgradePluginUpdateExtensions implements IPSUpgradePlugin
{
   /**
    * Implements process method of IPSUpgardePlugin.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("PSUpgradePluginUpdateExtensions.process()");
      PSExtensionInstallTool mgr = null;
      try
      {
         mgr = new PSExtensionInstallTool(new File(RxUpgrade.getRxRoot()));
         IPSExtensionManager emgr = mgr.getExtensionManager();
         handleResultDocProcessorExits(emgr);
         handleUdfProcFieldValidations(emgr);
         handleUdfProcFieldTransformers(emgr);
        log("Done successfully, updating extensions");
      }
      catch (PSExtensionException e)
      {
         log("Extension exception occurred:\n " + e.getLocalizedMessage());
         e.printStackTrace(config.getLogStream());
      }
      catch (IOException e)
      {
         log("IO  exception occurred:\n " + e.getLocalizedMessage());
         e.printStackTrace(config.getLogStream());
      }
      
      return null;
   }

   
   /**
    * Modify the set of extension names provided in the extList:
    * modification is to add the interface to the list of interfaces if not 
    * already present
    * @param mgr the extension manager never <code>null</code>
    * @param ifPattern the interface pattern as a filter on extensions
    * @param extSet Set of extension names may be empty but never 
    *    <code>null</code>
    * @param ifName the interface name that needs to be added, never 
    *    <code>null</code>
    */
   private void updateExtensions(IPSExtensionManager mgr, String ifPattern,
         Set extSet, String ifName) throws PSExtensionException
   {
      Iterator refs = null;      
      refs = mgr.getExtensionNames("Java", null, ifPattern, null);
      while ( refs.hasNext() )
      {
         PSExtensionRef ref = (PSExtensionRef)refs.next();
         try
         {
            IPSExtensionDef def = null;
            if ( extSet.contains(ref.getExtensionName()) )
            {
               def = mgr.getExtensionDef(ref);
               Iterator resources = def.getResourceLocations();
               boolean exists = def.implementsInterface(ifName);
               if ( !exists )
               {
                  Collection ifCol = new ArrayList();
                  CollectionUtils.addAll(ifCol, def.getInterfaces());
                  ifCol.add(ifName);
                  ((PSExtensionDef)def).setInterfaces(ifCol);
                  mgr.updateExtension(def, resources);
                  log("    Adding " + ifName + " to extension: "
                        + ref.getExtensionName());
               }
            }
         }
         catch (PSNotFoundException e)
         {
            log("Appropriate extension handler does not exist:"
                  + e.getLocalizedMessage());
         }
      }
   }
   
   
   /**
    * Add IPSItemValidator interface to the extension references specified in 
    * <code>m_itemValidatorList</code>
    * @param mgr
    * @throws PSExtensionException 
    */
   private void handleResultDocProcessorExits(IPSExtensionManager mgr)
         throws PSExtensionException
   {
      initItemValidators();
      log("----------------------------------------------------------");
      log("Adding interface: " + ITEM_VALIDATOR
            + " --> \n extensions implementing interface "
            + PATTERN_RESULT_DOC_PROC);
      updateExtensions(mgr, PATTERN_RESULT_DOC_PROC, m_itemValidators,
            ITEM_VALIDATOR);
   }

   
   /**
    * Add IPSFieldValidator interface to the extension references specified in 
    * <code>m_fieldValidators</code>
    * @param mgr the extension manager never <code>null</code>
    * @throws PSExtensionException 
    */

   private void handleUdfProcFieldValidations(IPSExtensionManager mgr)
         throws PSExtensionException
   {
      initFieldValidators();
      log("----------------------------------------------------------");
      log("Adding interface: " + FIELD_VALIDATOR
            + " --> \n extensions implementing interface "
            + PATTERN_UDF_PROC);
      updateExtensions(mgr, PATTERN_UDF_PROC, m_fieldValidators,
            FIELD_VALIDATOR);
   }


 
   /**
    * Add IPSFieldTransformer interface to the extension references specified in 
    * <code>m_fieldTransformers</code>
    * @param mgr the extension manager never <code>null</code>
    * @throws PSExtensionException 
    */

   private void handleUdfProcFieldTransformers(IPSExtensionManager mgr)
         throws PSExtensionException
   {
      initFiledTransformers();

      log("----------------------------------------------------------");
      log("Adding interface: " + FIELD_TRANSFORMER
            + " --> \n extensions implementing interface "
            + PATTERN_UDF_PROC);
      updateExtensions(mgr, PATTERN_UDF_PROC, m_fieldTransformers,
            FIELD_TRANSFORMER);
   }


   /**
    * Convenience method to add any of the extensions that need to implement
    * IPSFieldOutputTransformer
    */
   private void initFiledTransformers()
   {
      m_fieldTransformers.add("sys_MapOutputValue");
      m_fieldTransformers.add("sys_ToHash");
      m_fieldTransformers.add("sys_Trim");
      m_fieldTransformers.add("sys_ToProperCase");
      m_fieldTransformers.add("sys_Multiply");
      m_fieldTransformers.add("sys_Replace");
      m_fieldTransformers.add("sys_Add");
      m_fieldTransformers.add("sys_DateFormat");
      m_fieldTransformers.add("sys_DateFormatEx");
      m_fieldTransformers.add("sys_Subtract");
      m_fieldTransformers.add("sys_ToUpperCase");
      m_fieldTransformers.add("sys_ToLowerCase");
      m_fieldTransformers.add("sys_TextToXml");
      m_fieldTransformers.add("sys_OverrideLiteral");
      m_fieldTransformers.add("sys_FormatDate");
      m_fieldTransformers.add("sys_MapOutputValue");
   }

   /**
    * Convenience method to add any of the extensions that need to implement
    * IPSFieldValidator
    */
   private void initFieldValidators()
   {
      m_fieldValidators.add( "sys_GetBase64EncodedBody");
   }

   /**
    * Convenience method to add any of the extensions that need to implement
    * IPSItemValidator
    */
   private void initItemValidators()
   {
      m_itemValidators.add( "sys_comAddDefaultCommunity");
      m_itemValidators.add( "sys_casAutoRelatedContent");
      m_itemValidators.add( "sys_psxAddServerConfigParams");
      m_itemValidators.add( "sys_addAllowableWorkflowsForContentType");
      m_itemValidators.add( "sys_checkOutSlotItem");
      m_itemValidators.add( "sys_AddIsManagedNavUsed");      
   }

   /**
    * Prints message to the log printstream if it exists or just sends it to
    * System.out
    * 
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   /**
    * The upgrade context information
    */
   private IPSUpgradeModule m_config;
   
   
   /**
    * List of extension names for which a new interface must be added
    */
   private Set m_itemValidators = new HashSet();
   
   /**
    * List of extension names for which a new interface must be added
    */
   private Set m_fieldValidators = new HashSet();

   /**
    * List of extension names for which a new interface must be added
    */
   private Set m_fieldTransformers = new HashSet();

   
   /**
    * A string defining the interface that needs to be added
    */
   private static final String ITEM_VALIDATOR = 
               "com.percussion.extension.IPSItemValidator";

   /**
    * A string defining the interface that needs to be added
    */
   private static final String FIELD_VALIDATOR = 
               "com.percussion.extension.IPSFieldValidator";

   /**
    * A string defining the interface that needs to be added
    */
   private static final String FIELD_TRANSFORMER = 
               "com.percussion.extension.IPSFieldOutputTransformer";
   
   /**
    * A pattern to match extensions that extend IPSResultDocumentProcessor
    */
   private static final String PATTERN_RESULT_DOC_PROC = 
                     "com.percussion.extension.IPSResultDocumentProcessor";
   
   /**
    * A pattern to match extensions that extend IPSUdfProcessor
    */
   private static final String PATTERN_UDF_PROC = 
      "com.percussion.extension.IPSUdfProcessor";

}

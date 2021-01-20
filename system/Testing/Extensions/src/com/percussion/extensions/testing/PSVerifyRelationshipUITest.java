/******************************************************************************
 *
 * [ PSVerifyRelationshipUITest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit is used to verify the relationship workbench UI test. It assumes
 * that all 'system' relationships were readded as 'user' relationships with
 * the exact same values except the display and internal names. These names 
 * are assumed to be prependend with 'Test'.
 */
public class PSVerifyRelationshipUITest extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#processResultDocument(Object[], 
    *    IPSRequestContext, Document)
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // prepare result document
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("TestWorkbenchRelationshipEditor");
      doc.appendChild(root);
      
      PSRelationshipConfigSet configurations = 
         PSRelationshipCommandHandler.getConfigurationSet();
      
      Set systemConfigs = new HashSet();
      Map userConfigs = new HashMap();
      Iterator configs = configurations.iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (config.isSystem())
            systemConfigs.add(config);
         else
            userConfigs.put(config.getName(), config);
      }
      
      /*
       * Now verify that for each system relationship an equal user
       * relationship exists with 'Test' prependet to display and internal
       * names.
       */
      Iterator walker = systemConfigs.iterator();
      while (walker.hasNext())
      {
         PSRelationshipConfig systemConfig = 
            (PSRelationshipConfig) walker.next();
         
         String userConfigName = "Test" + systemConfig.getName();
         String userConfigLabel = "Test" + systemConfig.getLabel();
         
         PSRelationshipConfig userConfig = 
            (PSRelationshipConfig) userConfigs.get(userConfigName);
         
         if (userConfig != null)
         {
            PSRelationshipConfig systemClone = 
               (PSRelationshipConfig) systemConfig.clone();
            systemClone.setName(userConfigName);
            systemClone.setLabel(userConfigLabel);
            
            if (!systemClone.equals(userConfig))
               appendResult(doc, systemConfig.getLabel(), 
                  "Test relationship is not equal to system relationship!");
            else
               appendResult(doc, systemConfig.getLabel(), "Test O.K.!");
         }
         else
            appendResult(doc, systemConfig.getLabel(), 
               "No test relationship found for this system relationship!");
      }
      
      return doc;
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * Appends a new <code>Result</code> element to the document element of the 
    * supplied document with the supplied information as attributes.
    * 
    * @param doc the document to which the new result element will be appended,
    *    assumed not <code>null</code>.
    * @param configName the configuration for which to append a result,
    *    assumed not <code>null</code> or empty.
    * @param message the message of the results element, assumed not 
    *    <code>null</code> or empty.
    */
   private void appendResult(Document doc, String configName, String message)
   {
      Element result = doc.createElement("Result");
      result.setAttribute("relationship", configName);
      result.setAttribute("message", message);
      
      doc.getDocumentElement().appendChild(result);
   }
}

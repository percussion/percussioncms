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
package com.percussion.pso.imageedit.services;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.imageedit.data.ImageEditorException;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Template expander for image sizes.  Returns the list of binary templates that match the 
 * image sizes defined in an item. 
 *
 * @author DavidBenua
 *
 */
public class ImageSizeTemplateExpander extends AbstractTemplateExpanderAdaptor
      implements
         IPSTemplateExpander
{
   private static final Logger log = LogManager.getLogger(ImageSizeTemplateExpander.class);
   
   private ImageSizeDefinitionManager isdm = null; 
   private IPSAssemblyService asm = null; 
   /**
    *  Default constructor.
    */
   public ImageSizeTemplateExpander()
   {
      super.setNeedsContentNode(true); 
   }
   
   private void initServices()
   {
      if(isdm == null)
      {
         isdm = ImageSizeDefinitionManagerLocator.getImageSizeDefinitionManager();
      }
      if(asm == null)
      {
         asm = PSAssemblyServiceLocator.getAssemblyService(); 
      }
   }
   /**
    * Finds the templates for an image item. Only the contentNode parameter is examined, and 
    * it must not be <code>null</code>. 
    * @see AbstractTemplateExpanderAdaptor#findTemplates(IPSGuid, IPSGuid, IPSGuid, int, PSComponentSummary, Node, Map)
    */
   @Override
   protected List<IPSGuid> findTemplates(IPSGuid itemGuid, IPSGuid folderGuid,
         IPSGuid siteGuid, int context, PSComponentSummary summary,
         Node contentNode, Map<String, String> parameters)
   {
      initServices();
      String emsg; 
      String nodename = isdm.getSizedImageNodeName();
      String sizepropname= isdm.getSizedImagePropertyName(); 
      
      if(contentNode == null)
      {
         emsg = "The content node must be supplied";
         log.error(emsg);
         throw new ImageEditorException(emsg);
      }
      List<IPSGuid> tList = new ArrayList<IPSGuid>();
      
      try
      {
         NodeIterator children = contentNode.getNodes(nodename);
         while(children.hasNext())
         {
            Node child = children.nextNode();
            String sizecode = child.getProperty(sizepropname).getString();
            log.debug("found image size code {}", sizecode);
            if(StringUtils.isNotBlank(sizecode))
            {
                ImageSizeDefinition sizedef = isdm.getImageSize(sizecode); 
                if(sizedef != null)
                {
                   String templatename = sizedef.getBinaryTemplate(); 
                   IPSAssemblyTemplate template = asm.findTemplateByName(templatename);
                   tList.add(template.getGUID()); 
                }
                else
                {
                    log.debug("size code not found {}", sizecode);
                }
            }
            else
            {
               log.debug("child row has no sizecode");
            }
         }
      } catch (Exception ex)
      {
          log.error("Unexpected Exception Error: {}", ex.getMessage());
          log.debug(ex.getMessage(),ex);
          throw new ImageEditorException(ex);
      } 
      
      return tList; 
   }

   /**
    * Sets the image size definition manager. Used only for unit test.
    * @param isdm the isdm to set
    */
   protected void setIsdm(ImageSizeDefinitionManager isdm)
   {
      this.isdm = isdm;
   }

   /**
    * Sets the assembly service. Used only for unit test.
    * @param asm the asm to set
    */
   protected void setAsm(IPSAssemblyService asm)
   {
      this.asm = asm;
   }
}

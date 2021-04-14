/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   Log log = LogFactory.getLog(ImageSizeTemplateExpander.class);
   
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
            log.debug("found image size code " + sizecode);
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
                    log.debug("size code not found " + sizecode); 
                }
            }
            else
            {
               log.debug("child row has no sizecode");
            }
         }
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
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

/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAANodeType;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.server.PSServer;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action to get the template image urls. Expects sys_contenttypeid and objectId
 * parameters, throws exception if they are not part of supplied params. Gets
 * the templates for the supplied content type and then returns
 * 
 * JSONArray of JSONObjects with the template details. The JSONObject consists
 * of the following parameters.
 * 
 * <pre>
 *     templateId
 *     templateName
 *     thumbUrl
 *     fullUrl
 * </pre>
 * 
 * If the supplied objectId is of type item or slot then filters the templates
 * by slot.
 */
public class PSGetTemplateImagesForContentTypeAction extends PSAAActionBase
{

   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      Object obj = getParameter(params, IPSHtmlParameters.SYS_CONTENTTYPEID);
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '"
               + IPSHtmlParameters.SYS_CONTENTTYPEID
               + "' is required and cannot be empty for this action");
      }
      String cTypeId = obj.toString();
      PSAAObjectId objectId = getObjectId(params);
      String results = null;
      try
      {
         JSONArray array = new JSONArray();
         // Get the templates
         Collection<IPSAssemblyTemplate> templates = 
            new ArrayList<IPSAssemblyTemplate>();

         if (objectId.getNodeType() != null
               && (objectId.getNodeType().equals(
                     "" + PSAANodeType.AA_NODE_TYPE_SLOT.getOrdinal()) || objectId
                     .getNodeType().equals(
                           "" + PSAANodeType.AA_NODE_TYPE_SNIPPET.getOrdinal())))
         {
            templates = PSGetItemTemplatesForSlotAction.getAssociatedTemplates(
                  cTypeId, objectId.getSlotId());
         }
         else
         {
            IPSAssemblyService aService = PSAssemblyServiceLocator
                  .getAssemblyService();
            IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
            IPSGuid ctGuid = mgr.makeGuid(cTypeId, PSTypeEnum.NODEDEF);
            templates = aService.findTemplatesByContentType(ctGuid);
            templates = filterTemplatesBySite(templates,objectId.getSiteId());
            templates = getSortedTemplates(templates);
         }

         Map<String, String> fileNames = getImageFileNames();
         String siteid = objectId.getSiteId();
         IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
         IPSSite site = sm.loadUnmodifiableSite(PSGuidManagerLocator.getGuidMgr()
               .makeGuid(siteid, PSTypeEnum.SITE));
         String siteName = site.getName();
         for (IPSAssemblyTemplate template : templates)
         {
            JSONObject jobj = new JSONObject();
            jobj.append("templateId", template.getGUID().getUUID());
            jobj.append("templateName", template.getLabel());
            jobj.append("thumbUrl", getImageUrl(template.getName(),
                  siteName, "Thumb", fileNames));
            jobj.append("fullUrl", getImageUrl(template.getName(),
                  siteName, "Full", fileNames));
            array.put(jobj);
         }
         results = array.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(results, PSActionResponse.RESPONSE_TYPE_JSON);
   }

   /**
    * Filters the templates by supplied site.
    * 
    * @param templates If <code>null</code> returns <code>null</code>.
    * @param siteId if <code>null</code> templates are returned unfiltered.
    * @return Filtered templates by site. May be <code>null</code> or empty.
    * @throws PSMissingBeanConfigurationException
    * @throws PSSiteManagerException
    */
   private Collection<IPSAssemblyTemplate> filterTemplatesBySite(
         Collection<IPSAssemblyTemplate> templates, String siteId)
           throws PSSiteManagerException, PSMissingBeanConfigurationException, PSNotFoundException {
      if (templates == null)
         return null;
      if (siteId == null)
         return templates;
      IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sm.loadUnmodifiableSite(PSGuidManagerLocator.getGuidMgr()
            .makeGuid(siteId, PSTypeEnum.SITE));
      Set<IPSAssemblyTemplate> siteTempls = site.getAssociatedTemplates();
      templates.retainAll(siteTempls);
      return templates;
   }

   /**
    * Helper method to sort the supplied templates by outputformat and then by
    * name.
    * 
    * @param templates assumed not <code>null</code>.
    * @return sorted collection of templates by outputformat and by type, may be
    *         empty but never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Collection<IPSAssemblyTemplate> getSortedTemplates(
         Collection<IPSAssemblyTemplate> templates)
   {
      Map<OutputFormat, List<IPSAssemblyTemplate>> temp = 
         new HashMap<OutputFormat, List<IPSAssemblyTemplate>>();
      for (IPSAssemblyTemplate template : templates)
      {
         OutputFormat of = template.getOutputFormat();
         List<IPSAssemblyTemplate> tl = temp.get(of);
         if (tl == null)
         {
            tl = new ArrayList<IPSAssemblyTemplate>();
            temp.put(of, tl);
         }
         tl.add(template);
      }
      List<IPSAssemblyTemplate> sortedTempls = new ArrayList<IPSAssemblyTemplate>();
      List<OutputFormat> ofList = new ArrayList<OutputFormat>();
      ofList.add(OutputFormat.Global);
      ofList.add(OutputFormat.Page);
      ofList.add(OutputFormat.Snippet);
      ofList.add(OutputFormat.Binary);
      ofList.add(OutputFormat.Database);
      for (OutputFormat format : ofList)
      {
         List<IPSAssemblyTemplate> tlist = temp.get(format);
         if(tlist==null)
            continue;
         Collections.sort(tlist, new Comparator()
         {
            public int compare(Object obj1, Object obj2)
            {
               IPSAssemblyTemplate temp1 = (IPSAssemblyTemplate) obj1;
               IPSAssemblyTemplate temp2 = (IPSAssemblyTemplate) obj2;

               return temp1.getLabel().compareTo(temp2.getLabel());
            }
         });
         sortedTempls.addAll(tlist);
         
      }
      return sortedTempls;
   }

   /**
    * Helper method to build the image url for the supplied parameters.
    * 
    * <pre>
    *      Gets the image file name by the following algorithm.
    *      If the supplied map of fileNames is empty then returns the default system 
    *        image urls.
    *      Checks fileNames map with template_site_type as key if exists
    *        builds the url with that file name. 
    *      If not checks with template_ANY_type as key if exists builds the url 
    *        with that file name.
    *      If not checks with ANY_site_type as key  if exists builds the url with 
    *        that file name.
    *      If not checks with ANY_ANY_type as key  if exists builds the url with 
    *        that file name.
    *      If not builds the url with system defaults.
    *      Example: For a template rffImage and site EI for type of Thumb, 
    *      If an image exists under EI folder with name rffImage_Thumb then that
    *      image is returned. 
    *      Otherwise, If an image exists under EI folder with  AnyTemplate_Thumb 
    *         then that image is returned.
    *      Otherwise, If an image exists under AnySite folder with rffImage_Thumb 
    *         then that image is returned.
    *      Otherwise, If an image exists under AnySite folder with 
    *         AnyTemplate_Thumb then that image is returned.
    *      Otherwise, system default thumb image is returned.
    * </pre>
    * 
    * @param template name of the template, assumed not <code>null</code>.
    * @param site name of the site, assumed not <code>null</code>.
    * @param type assumed not <code>null</code> and to be one of
    *           ["Thumb","Full"]
    * @param fileNames assumed not <code>null</code> may be empty. See
    *           {@link #getImageFileNames()} description of expected entrties.
    * @return url of the image never <code>null</code>. relative to the
    *         Rhythmyx root. Ex: rx_resources/images/TemplateImages/Corporate
    *         Investments/rffPgCiGeneric_Thumb.jpg
    */
   private String getImageUrl(String template, String site, String type,
         Map<String, String> fileNames)
   {
      String imgUrl = type.equals("Thumb")
            ? sysDefaultThumbImage
            : sysDefaultFullImage;
      if (fileNames.isEmpty())
      {
         return imgUrl;
      }
      String imgName = template + "_" + site + "_" + type;
      String iurl = fileNames.get(imgName);
      if (iurl == null)
         iurl = fileNames.get(template + "_" + WILD_CARD_ANY_SITE + "_" + type);
      if (iurl == null)
         iurl = fileNames.get(WILD_CARD_ANY_TEMPLATE + "_" + site + "_" + type);
      if (iurl == null)
         iurl = fileNames.get(WILD_CARD_ANY_TEMPLATE + "_" + WILD_CARD_ANY_SITE
               + "_" + type);
      return iurl != null ? rxImageRoot + "/" + iurl : imgUrl;
   }

   /**
    * Helper method to build a map of filename key and value as file path. Walks
    * through all the folders under {@link #rxImageRoot} folder. Picks the files
    * that end with either _Thumb and _Full. Creates a key by inserting parent
    * foldername just before _Thumb or _Full in file name. The extension of file
    * is omitted in the key. The value is the set with the path of the file with
    * respect to {@link #rxImageRoot} folder.
    * 
    * @return Map of filename key and location may be empty, never
    *         <code>null</code>.
    */
   private Map<String, String> getImageFileNames()
   {
      Map<String, String> fileNames = new HashMap<String, String>();
      String imgFolderStr = PSServer.getRxDir() + File.separator + rxImageRoot;
      File imgFolder = new File(imgFolderStr);
      if (!imgFolder.exists() || !imgFolder.isDirectory())
      {
         ms_log.info("Templage image folder " + imgFolderStr
               + " does not exist.");
         return fileNames;
      }
      File[] dirs = imgFolder.listFiles();
      //loop through the site folders
      for(File dir:dirs)
      {
         if (!dir.isDirectory())
         {
            continue;
         }
         File[] files = dir.listFiles();
         //loop through the files
         for (File file : files)
         {
            if(file.isDirectory())
            {
               //ignore folders
               continue;
            }
            String fnfull = file.getName();
            String fnonly = fnfull.substring(0, fnfull.lastIndexOf("."));
            String type = "";
            if(fnonly.endsWith("_Thumb"))
            {
               fnonly = fnonly.substring(0,fnonly.lastIndexOf("_Thumb"));
               type = "_Thumb";
            }
            else if(fnonly.endsWith("_Full"))
            {
               fnonly = fnonly.substring(0,fnonly.lastIndexOf("_Full"));
               type = "_Full";
            }
            else
            {
               //we care about only files that end with either Thumb or Full 
               continue;
            }
            //Build the image file name.
            String imgFilename = dir.getName() + "/" + fnfull;
            String imgFileKey = fnonly + "_" + dir.getName() + type;
            if (fileNames.get(imgFileKey) != null)
            {
               String msg = "More than one template image exists with same "
                     + "name '{0}' but different extension using '{1}'";
               Object[] args =
               {fnfull, fileNames.get(imgFileKey)};
               ms_log.info(MessageFormat.format(msg, args));
               continue;
            }
            fileNames.put(imgFileKey, imgFilename);
         }
      }
      return fileNames;
   }

   /**
    * Constant for template images root folder.
    */
   private static final String rxImageRoot = "rx_resources/images/TemplateImages";

   /**
    * Default full image location
    */
   private static final String sysDefaultFullImage = 
      "sys_resources/images/TemplateImages/Default_Full.jpg";

   /**
    * Default full image location
    */
   private static final String sysDefaultThumbImage = 
      "sys_resources/images/templateImages/Default_Thumb.jpg";

   /**
    * Logger to use, never <code>null</code>.
    */
   private static Log ms_log = LogFactory
         .getLog(PSGetTemplateImagesForContentTypeAction.class);

   /**
    * Wild card constant for any site
    */
   private static final String WILD_CARD_ANY_SITE = "AnySite";

   /**
    * Wild card constant for any site
    */
   private static final String WILD_CARD_ANY_TEMPLATE = "AnyTemplate";
}

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
package com.percussion.webservices;

import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for retrieving Template's image URLs.
 */
public class PSTemplateImageUtils
{
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
    * @param template name of the template, assumed not <code>null</code> or
    * empty..
    * @param site name of the site, assumed not <code>null</code> or empty. It
    * may be {@link #ANY_SITE} if the Template is not "belongs" to any
    * site.
    * @param isThumb is requesting a URL of thumb or full image. It is
    * <code>true</code> if requesting a URL of the thumb image.
    * @param fileNames the file name / path map, which is returned by
    * {@link #getImageFileNames()}. It must not be <code>null</code>, but
    * may be empty.
    * 
    * @return url of the image, never <code>null</code>. relative to the
    * Rhythmyx root. Ex: rx_resources/images/TemplateImages/Corporate
    * Investments/rffPgCiGeneric_Thumb.jpg
    */
   public static String getImageUrl(String template, String site,
         boolean isThumb, Map<String, String> fileNames)
   {
      if (StringUtils.isBlank(template))
         throw new IllegalArgumentException("template name may not be blank.");
      if (StringUtils.isBlank(site))
         throw new IllegalArgumentException("site name may not be blank.");
      if (fileNames == null)
         throw new IllegalArgumentException("fileNames may not be null.");
      
      String type = isThumb ? "Thumb" : "Full";
      String imgUrl = type.equals("Thumb")
            ? sysDefaultThumbImage
            : sysDefaultFullImage;
      if (fileNames.isEmpty())
      {
         return imgUrl;
      }
      String imgName = getMapKey(template, site, isThumb);
      String iurl = fileNames.get(imgName);
      if (iurl == null)
         iurl = fileNames.get(template + "_" + ANY_SITE + "_" + type);
      if (iurl == null)
         iurl = fileNames.get(ANY_TEMPLATE + "_" + site + "_" + type);
      if (iurl == null)
         iurl = fileNames.get(ANY_TEMPLATE + "_" + ANY_SITE
               + "_" + type);
      return iurl != null ? rxImageRoot + "/" + iurl : imgUrl;
   }

   /**
    * Gets the map key from the specified template, site and image type.
    * The returned value can be used to lookup the map that is returned 
    * from {@link #getImageFileNames()}.
    * 
    * @param template the name of the template, may be blank.
    * @param site the name of the site and template belongs to, may be blank.
    * @param isThumb <code>true</code> for a thumb sized image; otherwise
    * for a full sized image.
    * 
    * @return the map key as described above, never <code>null</code>.
    */
   public static String getMapKey(String template, String site, boolean isThumb)
   {
      return template + "_" + site + "_" + (isThumb ? "Thumb" : "Full");
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
    * <code>null</code>.
    */
   public static Map<String, String> getImageFileNames()
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
               ms_log.warn(MessageFormat.format(msg, args));
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
   public static final String rxImageRoot = "rx_resources/images/TemplateImages";

   /**
    * Default full image location
    */
   public static final String sysDefaultFullImage = 
      "sys_resources/images/TemplateImages/Default_Full.jpg";

   /**
    * Default full image location
    */
   public static final String sysDefaultThumbImage = 
      "sys_resources/images/templateImages/Default_Thumb.jpg";

   /**
    * Logger to use, never <code>null</code>.
    */
   public static Log ms_log = LogFactory
         .getLog(PSTemplateImageUtils.class);

   /**
    * Wild card constant for any site
    */
   public static final String ANY_SITE = "AnySite";

   /**
    * Wild card constant for any site
    */
   public static final String ANY_TEMPLATE = "AnyTemplate";

}

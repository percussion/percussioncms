/*[ PSImageIconLoader.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ***************************************************************************/
package com.percussion.cx;

import com.percussion.util.IOTools;
import org.apache.log4j.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

 /**
  * Helper class to load image icons from UIManager cache or archive.
  */
public class PSImageIconLoader extends UIManager
{
   static Logger log = Logger.getLogger(PSImageIconLoader.class);
   private static String rootPath = "../../../";
   /**
    * Cannot instantiate!!!
    */
   private PSImageIconLoader()
   {
   }

   /**
    * Loads the icon with the supplied key from the UI manager/archive/server.If
    * not found in UI Manager cache, loads from the archive or server.
    * @param iconKey If it starts with ".." or "http" or "https", it is loaded
    * from the server otherwise from the JAR file. For the latter case, it must
    * not include the extension ".gif" which will be added by default. Must not
    * be <code>null</code> or empty.
    * @param expanded <code>true</code> if the expanded icon is required. For
    * expanded icon, the iconkey is appended with "_EXPANDED". This is ignored
    * if the iconKey starts with ".." or "http" or "https".
    * @return loaded icon object, <code>null</code> if load failed for any reason.
    * @throws IllegalArgumentException if the parameter iconKey is <code>null</code>
    * or empty.
    */
   public static Icon loadIcon(String iconKey, boolean expanded, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      if(iconKey==null || iconKey.length()<1)
      {
         throw new IllegalArgumentException("iconKey must not be null or empty");
      }
      /*
       * If it is a URL load from the server
       */
      if(iconKey.startsWith("..") ||
         iconKey.startsWith("http:") ||
         iconKey.startsWith("https:"))
      {
         try
         {
            return applet.getApplet().getIcon(iconKey);
         }
         catch(Exception e)
         {
            applet.getApplet().debugMessage(e);
         }
      }
      else
      {
         if (PSAjaxSwingWrapperLocator.getInstance().isAjaxSwingEnabled())
         {
            log.debug("In loadIcon() in PSImageIconLoader for AjaxSwing. iconKey is : " + "../sys_resources/ajaxswing/images/" + iconKey + ".gif");
            try
            {
               return applet.getApplet().getIcon("../sys_resources/ajaxswing/images/" + iconKey + ".gif");
            }
            catch(Exception e)
            {
               applet.getApplet().debugMessage(e);
            }
         }
      }
      /*
       * else load from the JAR file
       */
      String keyExpanded = iconKey + "_EXPANDED";
      Icon icon = null;
      if(expanded) //try loading expanded icon
         icon = loadIcon(keyExpanded);
      if(icon == null) //expanded icon failed to load, try loading the normal one
         icon = loadIcon(iconKey);
      if(icon == null) //Even the nrmal one failed, load the default one.
         icon = loadIcon("Missing");
      //Store in the cache if not already stored
      if(icon != null)
      {
         if(expanded && get(keyExpanded)==null)
            put(keyExpanded, icon);
         else if(get(iconKey)==null)
            put(iconKey, icon);
      }
      return icon;
   }

   /**
    * Loads the image icon from or archive. Tries to load from cache first then
    * from archive. If load fails by either of these methods <code>null</code>
    * returned. The cache is not maintained by this method.
    * @param iconKey key name of the icon that is the name of the icon file
    * without the extension. The extension '.gif' is always added by the method.
    * Assumes the image file is located in 'images/' directory within the archive.
    * Must not be <code>null</code> or <code>empty</code>, in which case
    * <code>null<code> is returned.
    * @return icon object loaded from cache or archive, may be <code>null</code>.
    */
   public static Icon loadIcon(String iconKey)
   {
      Icon icon = getIcon(iconKey);
      if(icon != null) //available in the cache, return it
         return icon;
      //Load from the archive
      try
      {
         String newIconKey = iconKey;
         if (!newIconKey.contains("images/")){
            newIconKey = "images/" + newIconKey;
         }
         if(!newIconKey.contains(".gif")){
            newIconKey = newIconKey + ".gif";
         }
         InputStream in = PSImageIconLoader.class.getResourceAsStream(
                 rootPath + newIconKey);
         if(in == null){
            in = PSImageIconLoader.class.getResourceAsStream(newIconKey);
         }
         if(in != null)
         {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOTools.copyStream(in, out);
            icon = new ImageIcon(out.toByteArray());
         }
      }
      catch(Exception e)
      {
         //Ok to return null
      }
      return icon;
   }
}
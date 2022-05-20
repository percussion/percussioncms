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

package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Maintains a list of bitmaps that are shared by multiple objects of the same 
 * package. Rather than each object of the same class keeping a copy of the 
 * image it needs, it uses the bitmap manager instance for a package to get a 
 * reference to a single instance of the image. Any class that uses an imutable 
 * image that will possibly be used by more than 1 object should use this class.
 * <p>
 * Use {@link #getBitmapManager(Class) getBitmapManager} to obtain a manager 
 * that can load images relative to the package of the class.
 */
public class BitmapManager
{
   // constructors
   /**
    * Default constructor. Use getBitmapManager() to get the singleton instance
    * of the manager for the specified class.
    * 
    * @param cl the class based on which it resolves the file location for the
    * images that are loaded using this manager, assumed not <code>null</code>.
    */
   private BitmapManager(Class cl)
   {
      m_class = cl;
   }

   /**
    * Returns a singleton instance of the BitmapManager for the supplied class
    * instance. Creates it if it hasn't been created.
    * 
    * @param cl the class based on which it resolves the file location for the
    * images that are loaded using this manager, may not be <code>null</code>.
    * Supply the class to whose package the image file path is relative.
    * 
    * @throws IllegalArgumentException if cl is <code>null</code>
    */
   public static BitmapManager getBitmapManager(Class cl)
   {
      if(cl == null)
         throw new IllegalArgumentException("cl may not be null");
      
      BitmapManager theManager = (BitmapManager)m_classManagerMap.get(cl);
      
      if (null == theManager)
      {
         theManager = new BitmapManager( cl );
         m_classManagerMap.put(cl, theManager);
      }
      return theManager;
   }

   // operations

   /**
    * Returns an instance of the image specified by strFilename. It checks
    * in the local cache for a image with the same name (case sensitive). If
    * one is found, it is returned, otherwise the image is loaded and returned.
    *
    * @param  strFilename - name of the bitmap resource. 
    *
    * @throws MissingResourceException if the image file cannot be found. The
    * filename is returned as part of the detail message and as the key.
    */
   public ImageIcon getImage(String strFilename)
   {
      boolean bInCache = true;

      // check if image in cache
      ImageIcon Img = (ImageIcon) Map.get(strFilename);
      
      if (null == Img)
      {
         URL IconURL = m_class.getResource(strFilename);
         if (null == IconURL)
         {
            throw new MissingResourceException(sm_res.getString("LoadIconFail"),
                  "IconURL", strFilename);
         }
         Img = new ImageIcon(IconURL);
         bInCache = false;
      }
      
      //first lets wait
      while (MediaTracker.LOADING == Img.getImageLoadStatus())

      //now check the status
      if (MediaTracker.COMPLETE != Img.getImageLoadStatus())
      {
         String astrParam [] =
         {
            strFilename
         };
         throw new MissingResourceException(MessageFormat.format( 
               sm_res.getString("FileDesc"), astrParam ), "ImageIcon", strFilename);
      }
      // add it to the cache if not already present
      if (!bInCache)
         Map.put(strFilename, Img);
   
      return Img;
   }
   
   // variables

   /**
    * The map of the <code>Class</code> used by the manager to resolve the file
    * location of the image to load it and the <code>BitmapManager</code>. Adds
    * a new instance of this class to the map whenever {@link 
    * #getBitmapManager(Class) getBitmapManager} is called with a new 
    * <code>Class</code> instance.
    */
   private static Map m_classManagerMap = new HashMap();
   
   /**
    * The class that will be used to resolve the file location of the image to 
    * load it. Initialized in the constructor and never <code>null</code> or 
    * modified after that.
    */
   private Class m_class;
   
   /**
    * Estimate of the # of different images used by designer. We can do a little
    * research on this number when the program nears completion.
    */
   private HashMap Map = new HashMap(60);
   
   /**
    * The resource bundle to use by this class, initialized statically and never
    * <code>null</code> or modified after that.
    */
   static ResourceBundle sm_res = ResourceHelper.getResources();
}




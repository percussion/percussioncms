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

package com.percussion.cx.javafx;

import com.percussion.cx.PSSelection;
import com.percussion.cx.objectstore.PSMenuAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PSWindowManager
{
   private static PSWindowManager instance = new PSWindowManager();

   private Map<String, PSDesktopExplorerWindow> windows = new HashMap<String, PSDesktopExplorerWindow>();
   private Map<String, String> parents = new HashMap<String, String>();
   protected String mi_style = "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=1";
   
   private int defaultHeight = 400;
   private int defaultWidth = 780;
   
   private static final String ROOT_TARGET="_root";
   
   static Logger log = Logger.getLogger(PSWindowManager.class);

   int window_count = 0;

   private String last_opened;

   public static PSWindowManager getInstance()
   {
      return instance;
   }

   public void addRoot(PSDesktopExplorerWindow baseFrame)
   {
      instance.windows.put(ROOT_TARGET, baseFrame);
      baseFrame.setTarget(ROOT_TARGET);
   }
   
   private static final Class<?>[] WINDOW_CLASSES =
   {PSPopupAppletFrame.class, PSSimpleSwingBrowser.class};

   public PSDesktopExplorerWindow open(String parent, String url, String target, String specs, PSSelection mi_selection, PSMenuAction action)
   {
      // _blank, _parent _self, _top or name
      //  force creation of unique name when blank specified so we will store the entry and it can be removed on close
      if (target.equals("_blank"))
         target = this.getClass().getName() + "_" + window_count++;
       
      PSDesktopExplorerWindow window = windows.get(target);

      if (window == null)
      {

         for (Class<?> windowClass : WINDOW_CLASSES)
         {
            try
            {
               // Don't like too much that we have to create a new instance to check here
               window = (PSDesktopExplorerWindow) windowClass.newInstance();
               if (window != null && window.validateOpen(url, target, specs, mi_selection, action))
                  break;
            }
            catch (InstantiationException | IllegalAccessException e)
            {
               log.error("Cannot instantiate window class " + windowClass.getName(), e);
            }
         }
         if (window!=null);
            windows.put(target, window);
         last_opened = target;   
         log.debug("Loading " + url + " to new window with target " + target);

      }
      else
      {
         if (window.getUrl()!= null &&  target.equals(window.target) && (window.getUrl().equals(url)) || url.isEmpty() )
            return window;
      }
      
      if (window != null)
      {
         StringBuffer specbuffer = null;
         if (StringUtils.isEmpty(specs) || specs.equals("undefined"))
         {
            specs=mi_style;
         }  
         specbuffer = new StringBuffer(specs);

         if (!specs.contains("height="))
         {
            specbuffer.append(",height=");
            specbuffer.append(defaultHeight);
         } 
         if (!specs.contains("width="))
         {
            specbuffer.append(",width=");
            specbuffer.append(defaultWidth);

         }
         
         window.open(parent, url, target, specbuffer.toString(), mi_selection, action);
      }
      else
         log.error("Cannot open window for url " + url + " to target " + target);

      return window;
   }

   public void close(String name)
   {
      PSDesktopExplorerWindow window = windows.get(name);
      if (window != null)
      {
         window.setClosed(true);
      }

      SwingUtilities.invokeLater(() ->
      {
         if (window != null)
         {
            window.dispose();
            windows.remove(name);
            parents.remove(name);
         }
      });
   }

   public PSDesktopExplorerWindow openWithParent(String parent, String mi_actionurl, String mi_target, String mi_style,
         PSSelection selection, PSMenuAction action)
   {
	   try
       {
	      if (parent != null)
	      {
	         
	         URL baseUrl;
	        
	            PSDesktopExplorerWindow parentWindow = windows.get(parent);
	            
	            if (mi_actionurl.isEmpty() && mi_target.equals("_self")) {
	            	
	            	/*
	            	 * This condition is true only when the code in javascript wants to close the window.
	            	 * As javascript can't close the window which it has not opened, the workaround code is used.
	            	 * window.open('','_self').close(); - When application is running in DCE, the handle is lost after 
	            	 * opening the window. So this piece of code would simply close the window as intended.
	            	 */
	            	
	            	parentWindow.setClosed(true);
	            	parentWindow.closeDceWindow();
	            	
	            	return null;
	            }
	            
	            baseUrl = new URL(parentWindow.getUrl());
	            mi_actionurl = new URL(baseUrl, mi_actionurl).toString();
	      }
	      boolean addParent = true;
	      if (mi_target.equals("_parent"))
	      {
	         if (parents.containsKey(parent))
	         {
	            mi_target = parents.get(parent);
	            addParent = false;
	         }
	      } 
	      else if (StringUtils.isEmpty(parent) && StringUtils.isEmpty(mi_target) || mi_target.equals("_self"))
	      {
	         mi_target = parent;
	         addParent = false;
	      }
	       
	    
	      
	      PSDesktopExplorerWindow  window = open(parent, mi_actionurl, mi_target, mi_style, selection, action);
	      
	      if (window!=null && addParent)
	         parents.put(window.getTarget(), parent);
	      
	      return window;
      
      }
      catch (MalformedURLException e)
      {
         log.error("invalid url", e);
         return null;
      }
   }

   public PSDesktopExplorerWindow getWindow(String target)
   {
      return windows.get(target);
   }

   public void windowResized(String target, int height, int width)
   {
      if (last_opened.equals(target))
      {
         defaultHeight=height;
         defaultWidth=width;
      }
      
   }

}

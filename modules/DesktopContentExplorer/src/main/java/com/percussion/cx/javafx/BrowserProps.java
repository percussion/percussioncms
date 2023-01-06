package com.percussion.cx.javafx;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class BrowserProps
{
   static Logger log = Logger.getLogger(BrowserProps.class);

   private boolean toolbar = false;
   private boolean location = false;
   private boolean directories = false;
   private boolean status = false;
   private boolean menubar = false;
   private boolean scrollbars = false;
   private boolean resizable = false;
   private int left = 200;
   private int height = 200;
   private int width = 200;

   public BrowserProps(String props)
   {
      String[] propList = StringUtils.split(props,",");

      for(String prop : propList)
      {
         String[] propEntryList = StringUtils.split(prop.trim(), "=");
         if (propEntryList.length==2)
         {
            String key = propEntryList[0].toLowerCase();
            String value = propEntryList[1];
            try {
               switch (key)
               {
                  case  "toolbar":
                     this.toolbar = booleanValue(value);
                     break;
                  case  "location":
                     this.location = booleanValue(value);
                     break;
                  case  "directories":
                     this.directories = booleanValue(value);
                     break;
                  case  "status":
                     this.status = booleanValue(value);
                     break;
                  case  "menubar":
                     this.menubar = booleanValue(value);
                     break;
                  case  "scrollbars":
                     this.scrollbars = booleanValue(value);
                     break;
                  case  "resizable":
                     this.resizable = booleanValue(value);
                     break;
                  case  "left":
                     this.left = intValue(value);
                     break;
                  case  "width":
                     this.width = intValue(value);
                     break;
                  case  "height":
                     this.height = intValue(value);
                     break;
               }
            }
            catch (Exception e)
            {
               log.debug("cannot parse window property "+key+" value="+value +" from string "+props);
            }
         }
      }

   }

   private int intValue(String value)
   {
      return Integer.parseInt(value);
   }

   private boolean booleanValue(String value)
   {
       value = value.toLowerCase();
       return value.equalsIgnoreCase("yes")|| value.equalsIgnoreCase("1");
   }

   public boolean isToolbar()
   {
      return this.toolbar;
   }

   public boolean isLocation()
   {
      return this.location;
   }

   public boolean isDirectories()
   {
      return this.directories;
   }

   public boolean isStatus()
   {
      return this.status;
   }

   public boolean isMenubar()
   {
      return this.menubar;
   }

   public boolean isScrollbars()
   {
      return this.scrollbars;
   }

   public boolean isResizable()
   {
      return this.resizable;
   }

   public int getLeft()
   {
      return this.left;
   }

   public int getHeight()
   {
      return this.height;
   }

   public int getWidth()
   {
      return this.width;
   }

}

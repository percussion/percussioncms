package com.percussion.cx;

import com.percussion.util.PSHttpConnection;

import java.net.MalformedURLException;



public class PSDefaultAjaxSwingWrapper implements IPSAjaxSwingWrapper
{

   public void createAjaxSwingHandlers(PSContentExplorerApplet applet)
   {
      // DO Nothing
      
   }
   

   public void openWindow(PSHttpConnection conn, String url, String target, String style) throws MalformedURLException
   {
      // DO Nothing
      
   }
   

   public void refreshWindow(PSHttpConnection conn) throws MalformedURLException
   {
      // DO Nothing
      
   }
   
 
   public boolean isAjaxSwingEnabled()
   {
      return false;
   }


   
}

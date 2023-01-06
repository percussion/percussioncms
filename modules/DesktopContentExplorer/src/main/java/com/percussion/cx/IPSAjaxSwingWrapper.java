package com.percussion.cx;

import com.percussion.util.PSHttpConnection;

import java.net.MalformedURLException;

public interface IPSAjaxSwingWrapper
{
   public void createAjaxSwingHandlers(PSContentExplorerApplet applet);

   public boolean isAjaxSwingEnabled(); 
   
   public void openWindow(PSHttpConnection conn, String url, String target, String style) throws MalformedURLException;

   public void refreshWindow(PSHttpConnection httpConnection) throws MalformedURLException;
}

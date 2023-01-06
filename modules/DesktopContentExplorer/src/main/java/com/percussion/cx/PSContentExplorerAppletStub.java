package com.percussion.cx;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PSContentExplorerAppletStub implements AppletStub, AppletContext
{
   PSContentExplorerHelper helper = new PSContentExplorerHelper();

   /**
    * hashmap containing all parameters needed for the applet
    */

   Map<String, String> parameters = new HashMap<String, String>();

   
   /**
    * Minimal implementation for AppletStub.
    *
    */
   public boolean isActive()
   {
      return false;
   }

   public URL getDocumentBase()
   {
      return null;
   }

   public URL getCodeBase()
   {
      return helper.getCodeBase();
   }

   public String getParameter(String key)
   {
     return  parameters.get(key);
   }

   public void setParemeter(String parameter, String value)
   {
      this.parameters.put(parameter, value);
   }

   public Map<String, String> getParameters()
   {
      return parameters;
   }

   public void setParameters(Map<String, String> map)
   {
      this.parameters = map;
   }

   public AppletContext getAppletContext()
   {
      return this;
   }

  

   /**
    * Minimal implementation for AppletContext.
    *
    */
   public AudioClip getAudioClip(URL url)
   {
      return null;
   }

   public Image getImage(URL url)
   {
      return null;
   }

   public Applet getApplet(String name)
   {
      return null;
   }

   public Enumeration getApplets()
   {
      return null;
   }

   public void showDocument(URL url)
   {
   }

   public void showDocument(URL url, String taget)
   {
   }

   public void showStatus(String status)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.applet.AppletContext#getStream(java.lang.String)
    */
   public InputStream getStream(String key)
   {
      // TODO - Implement for JDK 1.4
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public void setStream(String key, InputStream stream) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Iterator<String> getStreamKeys()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void appletResize(int width, int height)
   {
      // TODO Auto-generated method stub
      
   }
  
 

}
package com.percussion.cx.javafx;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class PSBrowserUtils
{
   public static void openWebpage(URI uri) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
              desktop.browse(uri);
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
  }

  public static void openWebpage(URL url) {
      try {
          openWebpage(url.toURI());
      } catch (URISyntaxException e) {
          e.printStackTrace();
      }
  }
  
  public static void main(String[] args) throws MalformedURLException{
     URL url = new URL("http://www.google.com");
     String pdf =  "http://localhost:9992/Rhythmyx/assembler/render?sys_revision=2&sys_authtype=0&sys_variantid=533&sys_context=0&sys_folderid=526&sys_siteid=303&sys_contentid=698&sys_command=edit";
     url = new URL(pdf);
     openWebpage(url);
  }

public static String toStringURL(String str)
{
   try
   {
      return new URL(str).toExternalForm();
   }
   catch (MalformedURLException exception)
   {
      return null;
   }
}
public static URL toURL(String str)
{
   try
   {
      return new URL(str);
   }
   catch (MalformedURLException exception)
   {
      return null;
   }
}
}

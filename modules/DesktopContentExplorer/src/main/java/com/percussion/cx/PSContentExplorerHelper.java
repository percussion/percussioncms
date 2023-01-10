package com.percussion.cx;

import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



public class PSContentExplorerHelper
{
   static Logger log = Logger.getLogger(PSContentExplorerHeader.class);
   
   public static  List<String> htmExt = Arrays.asList("html", "htm");
   
   public static  List<String> xlsExt = Arrays.asList("xls");
   
  
   private static ResourceBundle sm_res = null;

   public URL getCodeBase()
   {
      URL url = null;
      try
      {
         String host = "localhost";
         // String proto = documentBase.getProtocol();
         int port = 9992; // Integer.parseInt(
                        // getResources().getString("port"));
         url = new URL("http", host, port, "/Rhythmyx/sys_resources/AppletJars/");
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
      return url;
   }
   public static ResourceBundle getResources()
   {
      try
      {
         if (sm_res == null)
            sm_res = ResourceBundle.getBundle("com.percussion.cx.PSContentExplorerResources", Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         log.error(e);
      }

      return sm_res;
   }

   public  static ImageIcon  getImageIcon(Class clazz)
   {

      ImageIcon icon;
      try
      {
         icon = new ImageIcon(clazz.getResource(getResources().getString("gif_main")));
      }
      catch (Exception e1)
      {
         // TODO Auto-generated catch block
         icon = null;
      }
      return icon;

   }
   public static Map<String, String> initializeDefaultParameters()
   {
      Map<String,String> parameters = new HashMap<String,String>();
      
      // this var is important because we will check to see if we are invoking
      // this as a swing app
      parameters.put("SWING", "true");
      parameters.put("IMPACT", "false");

      // set defaults
      parameters.put("CODE", "com.percussion.cx.PSContentExplorerApplet.class");
      parameters.put("VIEW", "CX");
      parameters.put("RESTRICTSEARCHFIELDSTOUSERCOMMUNITY", "");
      parameters.put("CacheSearchableFieldsInApplet", "");
      parameters.put("isManagedNavUsed", "yes");
      parameters.put("CODEBASE", "../sys_resources/AppletJars");
      parameters.put("OPTIONS_URL", "../sys_cxSupport/options.xml");
      parameters.put("MENU_URL", "../sys_cx/ContentExplorerMenu.html");
      parameters.put("NAV_URL", "../sys_cx/ContentExplorer.html");
      parameters.put("CACHE_ARCHIVE", "ContentExplorer*.jar");
      parameters.put("CACHE_OPTION", "Plugin");
      parameters.put("ARCHIVE", "ContentExplorer*.jar");
      parameters.put("helpset_file", "../Docs/Rhythmyx/Business_Users/Content_Explorer_Help.hs");
      parameters.put("sys_cxinternalpath", "");
      parameters.put("sys_cxdisplaypath", "");
      parameters.put("TYPE", "application/x-java-applet;version=1.8.0_12");
      parameters.put("MAYSCRIPT", "true");
      parameters.put("NAME", "ContentExplorerApplet");
      parameters.put("ID", "ContentExplorerApplet");
      parameters.put("WIDTH", "960");
      parameters.put("HEIGHT", "700");
      parameters.put("LABEL", "Desktop Content Explorer");
    
      
      parameters.put("pssessionid", "");

      parameters.put("securitySOAPEndpoint", "/Rhythmyx/webservices/securitySOAP");

      return parameters;

   }
   public static Map<String, String>  initializeDTParameters(Map<String, String> parameters)
   {
     
      parameters.put("VIEW", "DT");
      parameters.put("CODE", "com.percussion.cx.PSContentExplorerApplet.class");
      parameters.put("OPTIONS_URL", "../sys_cxSupport/options.xml");
      parameters.put("MENU_URL", "../sys_cxDependencyTree/DependencyTreeMenu.html");
      parameters.put("NAV_URL", "../sys_cx/ContentExplorer.html");
      parameters.put("TITLE", "Impact Analysis");
      parameters.put("sys_cxinternalpath", "");
      parameters.put("sys_cxdisplaypath", "");
      parameters.put("NAME", "ContentExplorerApplet");
      parameters.put("ID", "ContentExplorerApplet");
      parameters.put("POPUP_TITLE", "Rhythmyx- Impact Analysis");
      return parameters;

   }
   public static Map<String, String>  initializeIAParameters(Map<String, String> parameters, String actionUrl)
   {
      String queryParams = "";
      String nav_url = "../sys_cxItemAssembly/itemassemblydata.html";
      int i = actionUrl.indexOf("?");
      if (i>=0){
       queryParams = actionUrl.substring(i+1);
       nav_url = nav_url+"?"+queryParams;
      }
      parameters.put("VIEW", "IA");
      parameters.put("CODE", "com.percussion.cx.PSContentExplorerApplet.class");
      parameters.put("OPTIONS_URL", "../sys_cxSupport/options.xml");
      parameters.put("MENU_URL", "../sys_cxItemAssembly/ItemAssemblyMenu.html");
      parameters.put("NAV_URL", nav_url);
  
      parameters.put("sys_cxinternalpath", "");
      parameters.put("sys_cxdisplaypath", "");
      parameters.put("NAME", "ContentExplorerApplet");
      parameters.put("ID", "ContentExplorerApplet");
      parameters.put("HEIGHT", "320");
      parameters.put("WIDTH", "782");
      parameters.put("POPUP_TITLE", "Active Assembly for Documents");
      
      return parameters;

   }
  
}
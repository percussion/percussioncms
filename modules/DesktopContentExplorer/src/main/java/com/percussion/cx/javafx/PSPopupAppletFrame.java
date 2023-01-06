package com.percussion.cx.javafx;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.cx.PSContentExplorerAppletStub;
import com.percussion.cx.PSContentExplorerApplication;
import com.percussion.cx.PSContentExplorerConstants;
import com.percussion.cx.PSContentExplorerHelper;
import com.percussion.cx.PSContentExplorerUtils;
import com.percussion.cx.PSSelection;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class PSPopupAppletFrame extends PSDesktopExplorerWindow
{
   static Logger log = Logger.getLogger(PSPopupAppletFrame.class);

   PSContentExplorerAppletStub stub = new PSContentExplorerAppletStub();

   private boolean windowLoaded;

   private String view = null;

   public PSPopupAppletFrame()
   {
      super();

      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(WindowEvent e)
         {
            setVisible(false);
            if (PSPopupAppletFrame.this.applet != null)
            {
               PSPopupAppletFrame.this.applet.stop();
               PSPopupAppletFrame.this.applet.destroy();

               PSWindowManager.getInstance().close(PSPopupAppletFrame.this.target);
            }
         }
      });

   }

   private static String getView(String mi_actionurl)
   {
      String view;
      if (mi_actionurl.contains("/Rhythmyx/sys_cxDependencyTree/dependencytree.html"))
         view = "DT";
      else if (mi_actionurl.contains("/sys_cxItemAssembly/itemassembly.html"))
         view = "IA";
      else
         return null;
      return view;
   }

   @Override
   public boolean validateOpen(String mi_actionurl, String mi_target, String mi_style, PSSelection selection,
         PSMenuAction actiom)
   {
      return getView(mi_actionurl) != null;
   }

   @Override
   public JFrame instanceOpen()
   {

      this.view = getView(this.mi_actionurl);
      
      if (this.applet != null)
      {
   
         this.applet.stop();
         this.applet.destroy();
         this.remove(this.applet);
      }
      
      this.applet = new PSContentExplorerApplet();
                  
      Platform.runLater(() -> {
         this.webView = new WebView();
         this.engine = this.webView.getEngine();
         this.engine.loadContent("<html></html>");
         setJavaBridge();
      });
      

      SwingUtilities.invokeLater(() -> {

         Map<String, String> params = buildSessionParameterMapForInnerApplet();

         setTitle(params.get(PSContentExplorerConstants.POPUP_TITLE));

         this.stub = new PSContentExplorerAppletStub();
         this.stub.setParameters(params);
         this.applet.setStub(this.stub);
         this.applet.setIsApplication(true);

         //  Need to create a webView to get javascript object for opener.

         this.browserProps = new BrowserProps(this.mi_style);

         this.add(this.applet, BorderLayout.NORTH);
         
         this.applet.init();
         this.applet.start();

         setVisible(true);

      });
      return this;

   }
   
   private Map<String, String> buildSessionParameterMapForInnerApplet()
   {

      Map<String, String> params = PSContentExplorerHelper.initializeDefaultParameters();
      if (this.view.equals("DT"))
         params.putAll(PSContentExplorerHelper.initializeDTParameters(params));
      else if (this.view.equals("IA"))
         params.putAll(PSContentExplorerHelper.initializeIAParameters(params, this.mi_actionurl));

      PSContentExplorerApplet baseapplet = PSContentExplorerApplication.getApplet();

      String sessionId = baseapplet.getParameter("pssessionid");
      String host = baseapplet.getParameter("serverName");
      String proto = baseapplet.getParameter("protocol");
      String port = baseapplet.getParameter("port");

      params.put("pssessionid", sessionId);
      params.put("serverName", host);
      params.put("protocol", proto);
      params.put("port", port);

      Map<String, String> queryParams = PSContentExplorerUtils.getQueryMap(this.mi_actionurl);
      // add all query params to the map
      queryParams.forEach(params::put);

      params.put(PSContentExplorerConstants.PARAM_CONTENTID, queryParams.get("sys_contentid"));
      params.put(PSContentExplorerConstants.PARAM_REVISIONID, queryParams.get("sys_revision"));
      params.put("LABEL", this.action.getLabel());

      if (this.selection.getNodeList() != null && this.selection.getNodeList().hasNext())
      {
         PSNode node = (PSNode) this.selection.getNodeList().next();
         params.put(PSContentExplorerConstants.PARAM_ITEM_TITLE, node.getName());

      }

      return params;
   }
   
   @Override
   public void reload(Map<String, String> parameters)
   {
      Map<String, String> params = buildSessionParameterMapForInnerApplet();
      //Don't use specific item unless when resetting unless specified
      params.remove(PSContentExplorerConstants.PARAM_CONTENTID);
      params.remove(PSContentExplorerConstants.PARAM_REVISIONID);
      
      if (parameters!=null)
         params.putAll(parameters);
      reload();
   }

}

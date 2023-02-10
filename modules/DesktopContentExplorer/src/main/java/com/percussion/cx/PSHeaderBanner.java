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

package com.percussion.cx;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.util.Set;

public class PSHeaderBanner extends JPanel
{

   private WebView webView = null;

   PSContentExplorerApplet applet = null;

   public PSHeaderBanner(PSContentExplorerApplet applet)
   {
      super();
      this.applet = applet;
      this.setFocusable(false);
      this.setFocusTraversalKeysEnabled(true);
      this.setLayout(new OverlayLayout(this));
      this.setName("Banner Image");
     
      // Create overlay to prevent JavaFx capturing events and accessibiltiy
      BannerJFXPanel banner = new BannerJFXPanel(applet);
      banner.setFocusable(false);
      banner.setFocusTraversalKeysEnabled(false);
      JPanel glass = new JPanel();
   
      glass.setName("Desktop Content Explorer Banner Image");
      glass.getAccessibleContext().setAccessibleName("Desktop Content Explorer Banner Image");
      glass.setFocusable(true);
      glass.requestFocusInWindow();
      glass.setFocusTraversalKeysEnabled(true);

      glass.setOpaque(false);
 
      banner.setLayout(new BorderLayout());
      this.add(banner, BorderLayout.CENTER);
      this.add(banner);
      this.add(glass);

   }

   class BannerJFXPanel extends JFXPanel
   {

      public BannerJFXPanel(PSContentExplorerApplet applet)
      {
         this.setPreferredSize(new Dimension(1000, 70));

         this.setFocusable(false);
         this.setFocusTraversalKeysEnabled(false);
         this.resetKeyboardActions();
         this.setEnabled(false);
         createScene(applet.getCodeBase());

      }

      private void createScene(URL base_url)
      {
         Platform.runLater(() -> {
            webView = new WebView();

            webView.setContextMenuEnabled(false);
            // hide webview scrollbars whenever they appear.

            webView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>()
            {
               @Override
               public void onChanged(Change<? extends Node> change)
               {
                  Set<Node> deadSeaScrolls = webView.lookupAll(".scroll-bar");
                  for (Node scroll : deadSeaScrolls)
                  {
                     scroll.setVisible(false);
                  }
               }
            });

            WebEngine engine = webView.getEngine();

            applet.setEngine(engine);

            String userAgent = engine.getUserAgent();
            userAgent += " PercussionDCE/0.0.0";
            engine.setUserAgent(userAgent);

            webView.setFocusTraversable(false);
            Scene scene = new Scene(webView);
            this.setScene(scene);

            engine.load(base_url + "../../dce_header.jsp");
            
            engine.getLoadWorker().stateProperty().addListener(new PSHyperlinkListener(webView));
         });
      }

   }

}

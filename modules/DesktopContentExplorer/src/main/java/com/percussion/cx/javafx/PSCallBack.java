package com.percussion.cx.javafx;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

public final class PSCallBack implements Callback<PopupFeatures, WebEngine>
{
   double width;
   double height;
   Stage popupStage = new Stage();

   private WebView popupWebView;

   private WebEngine engine;

   public PSCallBack(WebView popupWebView, double width, double heigth)
   {

      this.popupWebView = popupWebView;
      this.engine = popupWebView.getEngine();
      this.height = heigth;
      this.width = width;
      
   }

   @Override
   public WebEngine call(PopupFeatures popupFeatures)
   {

      Scene popupScene = null;
      if (popupWebView.getScene() != null){
         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
      if (popupWebView.getScene() == null)
      {
         popupScene = new Scene(getPopupWebView());
         getPopupWebView().prefWidthProperty().bind(popupScene.widthProperty());
         getPopupWebView().prefHeightProperty().bind(popupScene.heightProperty());
      }
      popupStage.setScene(popupWebView.getScene());
      popupStage.setResizable(popupFeatures.isResizable());
      popupStage.setWidth(width);
      popupStage.setHeight(height);
      popupStage.show();
      return getPopupWebView().getEngine();
   }

   public void setCloseEvent()
   {
      engine.setOnVisibilityChanged(new EventHandler<WebEvent<Boolean>>()
      {
         @Override
         public void handle(final WebEvent<Boolean> event)
         {

            // if event Data is set to false, means not visible.
            if (!event.getData())
            {
               popupStage.close();
           
            }

         }
      });
   }

   public void setPopupWebView(WebView popupWebView)
   {
      this.popupWebView = popupWebView;
   }

   public WebEngine getEngine()
   {
      return engine;
   }

   public void setEngine(WebEngine engine)
   {
      this.engine = engine;
   }

   public WebView getPopupWebView()
   {
      return popupWebView;
   }
}

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

import com.percussion.cx.javafx.PSDesktopExplorerWindow;
import com.percussion.cx.javafx.PSFileSaver;
import com.percussion.cx.javafx.PSWindowManager;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.concurrent.CountDownLatch;

public class PSJavaBridge implements ClipboardOwner {

   static Logger log = Logger.getLogger(PSJavaBridge.class);

   CountDownLatch initialized = new CountDownLatch(1);

   private PSDesktopExplorerWindow frame;

   public PSJavaBridge(PSDesktopExplorerWindow frame)
   {
      this.frame = frame;

   }

   public void log(String text)
   {
      log.debug("javascript.log: "+text);
   }

   public void error(String text)
   {
      log.debug("javascript.error: "+text);
   }

   public void closeWindow()
   {
      frame.setClosed(true);
      frame.closeDceWindow();
   }

   public void closeWindow(String windowName)
   {
      PSDesktopExplorerWindow window = PSWindowManager.getInstance().getWindow(windowName);
      window.setClosed(true);
      window.closeDceWindow();
   }

   public void saveFile(String binaryURL, String fileName) {
      PSFileSaver fileSaver = new PSFileSaver(binaryURL, fileName);
      fileSaver.startFileSaver();
   }

   public JSObject openWindow(String url, String name, String specs, boolean replace)
   {

      PSDesktopExplorerWindow window = frame.openChildWindow(url, name, specs, null, null);
      return window.getJSWindow();
   }

   public JSObject getWindowByName(String name)
   {
      PSDesktopExplorerWindow window = PSWindowManager.getInstance().getWindow(name);
      return window==null ? null : window.getJSWindow();
   }


   public JSClipDataBridge getClipboardData()
   {
      return new JSClipDataBridge();
         }

   public JSClipEventBridge getClipboardDataEvent()
   {
      return new JSClipEventBridge();
   }

   @Override
   public void lostOwnership(Clipboard clipboard, Transferable contents) {
      log.debug("Lost clipboard ownership");
   }
}

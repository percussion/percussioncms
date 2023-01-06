package com.percussion.cx;

public class JSClipEventBridge
{

   private boolean defaultPrevented = false;
   private boolean isImmediatePropagationStopped = false;
  
   // public for JS to see
   public JSClipDataBridge clipboardData;
   
   public JSClipEventBridge() {
      this.clipboardData = new JSClipDataBridge();
   }
   
   public boolean isDefaultPrevented()
   {
      return this.defaultPrevented;
   }
   
   public void preventDefault()
   {
      defaultPrevented=true;
   }
   
   public boolean isImmediatePropagationStopped()
   {
      return isImmediatePropagationStopped;
   }
}

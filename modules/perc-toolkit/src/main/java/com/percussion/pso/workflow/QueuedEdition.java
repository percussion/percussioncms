/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.workflow QueuedEdition.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;


/**
 * A Data Object describing an edition to be run in the future. 
 * As of 6.6, this is no longer needed. It is kept for compatibility 
 * purposes only. 
 *
 * @author DavidBenua
 * @deprecated use the IPSRxPublisherService classes directly instead. 
 */
public class QueuedEdition 
{
   private boolean isLocal = false;
   private String editionId; 
   private String sessionId = null; 
   private String cmsUser = null;
   private String cmsPassword = null; 
   private String uri; 
   private String listenerPort; 
   private int retryCount = 0; 
   /**
    * 
    */
   public QueuedEdition(String uri,String port, String editionId, boolean local, int retries )
   {
       this.uri = uri; 
       this.listenerPort = port;
       this.editionId = editionId;
       this.isLocal = local; 
       this.retryCount = retries; 
   }
  
   /**
    * @return Returns the cmsPassword.
    */
   public String getCmsPassword()
   {
      return cmsPassword;
   }
   /**
    * @param cmsPassword The cmsPassword to set.
    */
   public void setCmsPassword(String cmsPassword)
   {
      this.cmsPassword = cmsPassword;
   }
   /**
    * @return Returns the cmsUser.
    */
   public String getCmsUser()
   {
      return cmsUser;
   }
   /**
    * @param cmsUser The cmsUser to set.
    */
   public void setCmsUser(String cmsUser)
   {
      this.cmsUser = cmsUser;
   }
   /**
    * @return Returns the editionId.
    */
   public String getEditionId()
   {
      return editionId;
   }
   /**
    * @param editionId The editionId to set.
    */
   public void setEditionId(String editionId)
   {
      this.editionId = editionId;
   }
   /**
    * @return Returns the sessionId.
    */
   public String getSessionId()
   {
      return sessionId;
   }
   /**
    * @param sessionId The sessionId to set.
    */
   public void setSessionId(String sessionId)
   {
      this.sessionId = sessionId;
   }
   /**
    * @return Returns the uri.
    */
   public String getUri()
   {
      return uri;
   }
   /**
    * @param uri The uri to set.
    */
   public void setUri(String uri)
   {
      this.uri = uri;
   }
   /**
    * @return Returns the local flag.
    */
   public boolean isLocal()
   {
      return isLocal;
   }

   /**
    * @return Returns the retryCount.
    */
   public int getRetryCount()
   {
      return retryCount;
   }
   
   public boolean decrementAndTestRetries()
   {
      retryCount--;
      return(retryCount > 0);
   }

   /**
    * @return Returns the listenerPort.
    */
   public String getListenerPort()
   {
      return listenerPort;
   }
}

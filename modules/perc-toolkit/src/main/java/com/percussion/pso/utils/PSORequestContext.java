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
 * com.percussion.pso.utils PSORequestContext.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.util.IPSHtmlParameters;

/**
 * A system request that overrides the PSRequestContext. 
 * Use this class to obtain an IPSRequestContext for the
 * system user (RxServer).   
 *
 * @author DavidBenua
 *
 */
public class PSORequestContext extends PSRequestContext
      implements
         IPSRequestContext
{ 
   /**
    * Gets a the system user request.  
    * This system request is always forced to be local to the server, even if 
    * the original user request came from elsewhere. 
    */
   public PSORequestContext()
   {
      super(PSRequest.getContextForRequest(true));
   }
   
   /**
    * Gets the system user request, specifying a community. 
    * @param CommunityId
    */
   public PSORequestContext(String CommunityId)
   {
   	 this();
   	 this.setCommunity(CommunityId);
   }
   /**
    * This method always returns <code>false</code>. 
    * System requests cannot trace, beccause there is no home application. 
    * @see com.percussion.server.IPSRequestContext#isTraceEnabled()
    */
   public boolean isTraceEnabled()
   {
      return false;
   }
   
   /**
    * Sets the user community.  
    * @param communityId the Community Id to set. 
    */
   public void setCommunity(String communityId)
   {
   	super.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, communityId);
   }
  
}

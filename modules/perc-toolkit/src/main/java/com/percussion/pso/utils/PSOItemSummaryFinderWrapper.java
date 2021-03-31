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
 * com.percussion.pso.utils. PSOItemSummaryFinderWrapper.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.utils.guid.IPSGuid;

/**
 * Wrapper class for the static methods of PSOItemSummaryFinder.
 * Used to extract an interface for testability. 
 *
 * @author DavidBenua
 *
 */
public class PSOItemSummaryFinderWrapper implements IPSOItemSummaryFinder
{
   /**
    * Default constructor.
    */
   public PSOItemSummaryFinderWrapper()
   {
      
   }
   
   
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getCurrentOrEditLocator(com.percussion.utils.guid.IPSGuid)
    */
   public  PSLocator getCurrentOrEditLocator(IPSGuid guid) 
      throws PSException
   {
      return PSOItemSummaryFinder.getCurrentOrEditLocator(guid);
   }
    
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getCurrentOrEditLocator(java.lang.String)
    */
   public PSLocator getCurrentOrEditLocator(String contentId)
      throws PSException
   {
      return PSOItemSummaryFinder.getCurrentOrEditLocator(contentId);
   }
   
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getCurrentOrEditLocator(int)
    */
   public PSLocator getCurrentOrEditLocator(int id)
      throws PSException
   {
      return PSOItemSummaryFinder.getCurrentOrEditLocator(id);
   }
   
   public static final int CHECKOUT_NONE = 1; 
   public static final int CHECKOUT_BY_ME = 2; 
   public static final int CHECKOUT_BY_OTHER = 3; 
   
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getCheckoutStatus(java.lang.String, java.lang.String)
    */
   public int getCheckoutStatus(String contentId, String userName) 
      throws PSException
   {
      return PSOItemSummaryFinder.getCheckoutStatus(contentId, userName);
   }
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getSummary(java.lang.String)
    */
   public  PSComponentSummary getSummary(String contentId) 
      throws PSException
   {
      return PSOItemSummaryFinder.getSummary(contentId);
   }
  
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getSummary(com.percussion.utils.guid.IPSGuid)
    */
   public  PSComponentSummary getSummary(IPSGuid guid) 
   throws PSException
   {
      return PSOItemSummaryFinder.getSummary(guid);
   }
  
   /**
    * @see com.percussion.pso.utils.IPSOItemSummaryFinder#getSummary(int)
    */
   public  PSComponentSummary getSummary(int id) 
      throws PSException
   {
      return PSOItemSummaryFinder.getSummary(id);
   }

   
}



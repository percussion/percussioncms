/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.utils.guid.IPSGuid;
public interface IPSOItemSummaryFinder
{
   public PSLocator getCurrentOrEditLocator(IPSGuid guid) throws PSException;
   public PSLocator getCurrentOrEditLocator(String contentId)
         throws PSException;
   public PSLocator getCurrentOrEditLocator(int id) throws PSException;
   public int getCheckoutStatus(String contentId, String userName)
         throws PSException;
   /**
    * Gets the component summary for an item.
    * @param contentId the content id
    * @return the component summary. Never <code>null</code>.
    * @throws PSException when the item does not exist.
    */
   public PSComponentSummary getSummary(String contentId) throws PSException;
   public PSComponentSummary getSummary(IPSGuid guid) throws PSException;
   public PSComponentSummary getSummary(int id) throws PSException;
}
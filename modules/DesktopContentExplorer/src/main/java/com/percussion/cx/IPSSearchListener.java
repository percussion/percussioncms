/*[ IPSSearchListener.java ]****************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;

/**
 * This listener defines methods that are called during a search and after
 * a search. This allows the implementers to show information about the search
 * to the end user.
 */
public interface IPSSearchListener
{
   /**
    * Called when the user selects a new action. This allows the implementer
    * to remove any search displays.
    */
   void searchReset();
   
   /**
    * Called when a search is initiated.
    * @param node the search node, which may contain cached results,
    * must never be <code>null</code>.
    */
   void searchInitiated(PSNode node);
   
   /**
    * Called when a search completes.
    * @param node the search node, which contains the results, 
    * must never be <code>null</code>.
    */
   void searchCompleted(PSNode node);
}


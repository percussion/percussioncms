/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.relationships;
import java.util.List;
import java.util.Set;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;
public interface IPSOParentFinder
{
   /**
    * Finds all parents for an item.  Convenience method for {@link #findAllParents(PSLocator, String)}.. 
    * @param contentid the item content id
    * @param slotName the slot name
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>. 
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findAllParents(String contentid, String slotName)
         throws PSAssemblyException, PSCmsException;
   /**
    * Finds all parents for an item. Includes both parents with the current owner revision, the edit 
    * owner revision, and the last public revision. 
    * @param dependent the dependent item. 
    * @param slotName the slot name. Must not be null or empty. 
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findAllParents(PSLocator dependent, String slotName)
         throws PSAssemblyException, PSCmsException;
   /**
    * Find parents for an item. Convenience method for {@link #findParents(PSLocator, String, boolean)}. 
    * @param contentid the content id.  
    * @param slotName  the slot name.
    * @param usePublic the public revision flag. 
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findParents(String contentid, String slotName,
         boolean usePublic) throws PSAssemblyException, PSCmsException;
   /**
    * Finds the 
    * @param dependent the locator for the dependent item.
    * @param slotName
    * @param usePublic the public revision flag. If <code>true</code>, only relationships where the owner is 
    * the public revision will be considered.  If<code>false</code>, only relationships where the owner is the 
    * current or edit revision will be considered.  
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findParents(PSLocator dependent, String slotName,
         boolean usePublic) throws PSAssemblyException, PSCmsException;
   /**
    * Determines if this item has any non-public ancestors in the given slot name. 
    * Will return <code>false</code> if any direct or indirect ancestor item (in the given slot) 
    * is in a workflow state that does not have one of the valid flags in it. An item with no parents 
    * will return <code>true</code>.  
    * @param contentId the content id of the item. 
    * @param slotName the name of the slot.
    * @param validFlags the list of valid flags. 
    * @return <code>true</code> if all of the items ancestors in the slot are public. 
    * @throws PSAssemblyException
    * @throws PSException
    */
   public boolean hasOnlyPublicAncestors(String contentId, String slotName,
         List<String> validFlags) throws PSAssemblyException, PSException;
}
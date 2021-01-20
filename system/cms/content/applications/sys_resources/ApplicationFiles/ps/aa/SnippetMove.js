/******************************************************************************
 *
 * [ ps.aa.SnippetMove.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.aa.SnippetMove");

dojo.require("dojo.lang.assert");
dojo.require("dojo.lang.type");

/**
 * Data for moving a snippet between slots.
 * Read-only after creation.
 *
 * @param {ps.aa.ObjectId} snippetId the snippet id. Not null.
 * @param {ps.aa.ObjectId} targetSlotId the id of the slot to move
 * the snippet to. Not null.
 * @param {Number} targetIndex the position, starting with 1,
 * in the target slot to move the snippet to.
 * Not <code>null</code>.
 * @param {Boolean} dontUpdatePage if specified and <code>true</code>
 * the move operation should not update the page. Optional.
 * Can be used, for example, with DnD, to allow the drop operation
 * to update the page itself.
 * @constructor
 */
ps.aa.SnippetMove = function (snippetId, slotId, targetSlotId, targetIndex,
       dontUpdatePage)
{
   dojo.lang.assertType(snippetId, ps.aa.ObjectId);
   dojo.lang.assertType(targetSlotId, ps.aa.ObjectId);
   dojo.lang.assert(dojo.lang.isNumeric(targetIndex),
         "Should be interpreted as number");
   dontUpdatePage && dojo.lang.isBoolean(dontUpdatePage);

   this._m_snippetId = snippetId;
   this._m_slotId = slotId;
   this._m_targetSlotId = targetSlotId;
   this._m_targetIndex = targetIndex;
   this._m_dontUpdatePage = dontUpdatePage || false;

   this._m_targetSnippetId = snippetId.clone();
   this._m_targetSnippetId.setSlotId(targetSlotId.getSlotId());
   this._m_targetSnippetId.setSortRank(targetIndex);
   this._m_uiUpdateNeeded = false;
   this._m_success = undefined;

   /**
    * Returns the original snippet id, originally passed to the constructor.
    * @return the snippet id.
    * Not <code>null</code>.
    */
   this.getSnippetId = function ()
   {
      return this._m_snippetId;
   }
   
   /**
    * Returns snippet id after it is moved to the new slot.
    * @return the snippet id.
    * Not <code>null</code>.
    */
   this.getTargetSnippetId = function ()
   {
      return this._m_targetSnippetId;
   }
   
   /**
    * Specifies new target snippet id.
    * @param targetSnippetId the new target snippet id.
    * Not <code>null</code>.
    */
   this.setTargetSnippetId = function (targetSnippetId)
   {
      dojo.lang.assertType(targetSnippetId, ps.aa.ObjectId);
      this._m_targetSnippetId = targetSnippetId;
   }

   /**
    * Returns the source slot id, originally passed to the constructor.
    * @return the slot id.
    * Not <code>null</code>.
    */
   this.getSlotId = function ()
   {
      return this._m_slotId;
   }

   /**
    * Returns the destination slot id, originally passed to the constructor.
    * @return the destination slot id.
    * Not <code>null</code>.
    */
   this.getTargetSlotId = function ()
   {
      return this._m_targetSlotId;
   }
   
   /**
    * Returns the 1-based target position of the snippet in the slot,
    * returned by {@link #getTargetSlot}.
    * May be <code>null</code>, in which case the item will be appended
    * to the end of the slot items.
    */
   this.getTargetIndex = function ()
   {
      return this._m_targetIndex;
   }
   
   /**
    * Whether the move operation should also update the page.
    */
   this.getDontUpdatePage = function ()
   {
      return this._m_dontUpdatePage;
   }
   
   /**
    * Sets new value to the property returned by {@link #getDontUpdatePage}.
    */
   this.setDontUpdatePage = function (dontUpdatePage)
   {
      this._m_dontUpdatePage = dontUpdatePage;
   }
   
   /**
    * Equals to <code>true</code>, only if the move was performed, backend
    * data were updated, but the update is not reflected in UI yet,
    * because {@link #getDontUpdatePage} is <code>true</code>.
    */
   this.isUiUpdateNeeded = function ()
   {
      return this._m_uiUpdateNeeded;
   }
   
   /**
    * Sets new value returned by {@link #isUiUpdateNeeded}.
    * @param {Boolean} uiUpdateNeeded the new value. Can be <code>true</code>
    * only if {@link getDontUpdatePage} is <code>true</code>.
    * Not <code>null</code>.
    */
   this.setUiUpdateNeeded = function (uiUpdateNeeded)
   {
      dojo.lang.assert(dojo.lang.isBoolean(uiUpdateNeeded));
      if (uiUpdateNeeded)
      {
         dojo.lang.assert(this.getDontUpdatePage(),
               "uiUpdateNeedeed can be true only if getDontUpdatePage is true");
      }
      this._m_uiUpdateNeeded = uiUpdateNeeded;
   }
   
   /**
    * Indicates whether the move is successful.
    * @return <code>true</code> if the move operationwas successful,
    * <code>false</code> if it failed, <code>undefined</code> otherwise.
    */
   this.isSuccess = function ()
   {
      return this._m_success;
   }
   
   /**
    * Sets the value returned by {@link #isSuccess}.
    * @param {Boolean} success the new value.
    */
   this.setSuccess = function (success)
   {
      dojo.lang.assertType(success, Boolean);
      this._m_success = success;
   }
}

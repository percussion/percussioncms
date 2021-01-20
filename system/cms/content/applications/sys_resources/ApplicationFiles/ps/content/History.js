/******************************************************************************
 *
 * [ ps.content.History.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
dojo.provide("ps.content.History");

dojo.require("dojo.collections.Stack");


/**
 * A history data object. Acts as a stack of string entries.
 * Insures that it has at least one path, which is never removed or changed.
 * Top of the stack serves as the current entry.
 * @param {String} initialEntry the initial history entry, which is never removed.
 * @constructor
 */
ps.content.History = function (initialEntry)
{
   dojo.lang.assertType(initialEntry, String);

   /**
    * Stack of string paths visited by the user. The current path is on the top
    * of the stack. Never <code>null</code> or empty.
    */
   this._m_stack = new dojo.collections.Stack([initialEntry]);
   
   /**
    * Adds new entry to the history.
    * @param {String} newEntry new history entry.
    * Does nothing if the entry is the same as the entry returned by
    * {@link #getCurrent}.
    * Not <code>null</code>.
    */
   this.add = function (newEntry)
   {
      dojo.lang.assertType(newEntry, String);
      if (newEntry !== this.getCurrent())
      {
         this._m_stack.push(newEntry);
      }
   }

   /**
    * Removes the top stack entry,
    * if {@link #canGoBack} returns <code>true</code>.
    * Otherwise throws an assertion exception.
    * @return the next history entry. Not <code>null</code>.
    */
   this.back = function ()
   {
      dojo.lang.assert(this.canGoBack());
      return this._m_stack.pop();
   }

   /**
    * Indicates whether there are more entries in the history to go back to.
    */
   this.canGoBack = function ()
   {
      return this._m_stack.count > 1;
   }
   
   /**
    * The topmost history entry.
    */
   this.getCurrent = function ()
   {
      return this._m_stack.peek();
   }
}
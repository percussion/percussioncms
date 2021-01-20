
/******************************************************************************
 *
 * [ ps.widget.PSSplitContainer.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.widget.PSSplitContainer");

dojo.require("dojo.widget.SplitContainer");

/**
 * The SplitContainer class overriden to allow disabling of the sizer bar
 */
dojo.widget.defineWidget("ps.widget.PSSplitContainer", dojo.widget.SplitContainer,
{
    
    /**
     * Sizer enabled flag, if <code>true</code> which is the default then the
     * sizer bar will move when dragged.
     */
    sizerEnabled: true,
    
    /**
     * Flag indicating that the sizer should be visible.
     * Defaults to <code>true</code>
     */
    sizerVisible: true,    
    
    /**
     * Overridden method to control when sizing is allowed.
     * @see dojo.widget.SplitContainer#beginSizing for more detail
     */
    beginSizing: function(e, i)
    {
       if(this.sizerEnabled)
       {
          ps.widget.PSSplitContainer.superclass.beginSizing.apply(this, arguments);
       }
    },
    
    /**
     * Overriden to set the default cursor if the sizer is disabled
     */
    postCreate: function(args, fragment, parentComp)
    {
	ps.widget.PSSplitContainer.superclass.postCreate.apply(this, arguments);
	if(!this.sizerEnabled)
	{
	   this.virtualSizer.style.cursor = 'default';
	   for(i = 0; i < this.sizers.length; i++)
	   {
	      this.sizers[i].style.cursor = 'default';
	   }
	}
	if(!this.sizerVisible)
	{
	   this.virtualSizer.style.visibility = 'hidden';
	   for(i = 0; i < this.sizers.length; i++)
	   {
	      this.sizers[i].style.visibility = 'hidden';
	   }
	}
	
    }
    
});



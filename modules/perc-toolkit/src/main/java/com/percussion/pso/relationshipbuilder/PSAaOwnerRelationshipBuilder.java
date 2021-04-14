/******************************************************************************
 *
 * [ PSAaOwnerRelationshipBuilder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import static java.util.Collections.singleton;

import java.util.Collection;

import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

/**
 * Creates and deletes auto relationships where the parent item (item being
 * updated) is the <em>owner</em> of the relationship and the child items
 * (items to be related) are the <em>dependent</em>.
 * @author Adam Gent
 * @author James Schultz
 * @since 6.0
 * @see PSAaDependentRelationshipBuilder
 * @see #retrieve(int)
 */
public class PSAaOwnerRelationshipBuilder extends PSActiveAssemblyRelationshipBuilder {

	public PSAaOwnerRelationshipBuilder() {
		setParent(true);	
	}
}

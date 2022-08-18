/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.impl;

/**
 * Do the actual processing of inline content. Each element is examined for the
 * attribute rxinlineslot. If the attribute exists then the processor handles
 * that element (and contained elements for inline templates) differently.
 * <p>
 * The processor addresses each presented component in turn. It then exhibits
 * one of these behaviors:
 * <ul>
 * <li>Inline links (A and IMG elements): The current element is modified to
 * reference a new generated link. If the reference content item is not valid
 * for the context, the link is removed.</li>
 * <li>Inline templates: The element is passed through, with all content until
 * the end of the element ignored. That content is replaced by the assembly of
 * the template referenced.</li>
 * <li>Regular elements: The element is copied to the output if the handler is
 * in pass through mode, or swallowed in ignore mode.</li>
 * </ul>
 * <p>
 * The handler maintains a stack of elements and states. These enable the
 * processor to adjust the handling as it goes.
 * 
 * @author dougrand
 * @deprecated @see PSInlineLinkProcessor instead
 */
@Deprecated
public class PSInlineLinkContentHandler
{
}

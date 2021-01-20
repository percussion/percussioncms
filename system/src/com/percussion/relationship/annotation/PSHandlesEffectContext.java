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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.relationship.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define which contexts the effect handles and indicates new
 * request processing is supported.
 * <p>
 * <pre>
 * @PSHandlesEffectContext(
 *    required={PSEffectContext.PRE_CONSTRUCTION, PSEffectContext.PRE_UPDATE},
 *    optional={PSEffectContext.PRE_DESTRUCTION}
 *    )
 * public class DemoEffect implements IPSEffect 
 * {
 *    .....
 * }
 * </pre>
 * <p>
 * or 
 * <p>
 * <pre>
 * @PSHandlesEffectContext(
 *    required={PSEffectContext.PRE_WORKFLOW},
 *    endpoint={PSEndpoint.BOTH}
 *    )
 * public class DemoEffect implements IPSEffect 
 * {
 *    .....
 * }
 * </pre>
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PSHandlesEffectContext 
{
   /**
    * Required contexts will automatically added to relationship configuration
    * when the effect is added to a relationship type.
    * @return an array of {@link PSEffectContext}, never <code>null</code>, defaults to empty array
    */
   PSEffectContext[] required() default {};
   
   /**
    * Optional contexts will be allowed to be manually added to relationship configuration 
    * when the effect is added to a type,  contexts that are not in optional, or required will
    * be removed.  This defaults to ALL which allows any user assigned contexts
    * @return an array of {@link PSEffectContext}, never <code>null</code>, defaults to PSEffectContext.ALL
    */
   PSEffectContext[] optional() default {PSEffectContext.ALL};
   
   /**
    * The endpoint is only applicable to workflow contexts that are not directly related to 
    * effect construction, destruction and update, e.g. 
    * {@link PSEffectContext#PRE_WORKFLOW}, {@link PSEffectContext#POST_WORKFLOW}
    * {@link PSEffectContext#PRE_CHECKIN} and {@link PSEffectContext#POST_CHECKOUT}.
    * <p>
    * This defines which relationships of the item to process, e.g. whether the current item is
    * the owner of the relationship or dependent, or all relationships to and from the current item.
    * @return the {@link PSEndpoint}, never <code>null</code>, defaults to {@link PSEndpoint#USER}.
    */
   PSEndpoint endpoint() default PSEndpoint.USER;
   
}

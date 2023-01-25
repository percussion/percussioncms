/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

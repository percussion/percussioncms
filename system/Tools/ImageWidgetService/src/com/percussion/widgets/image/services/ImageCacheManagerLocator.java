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

package com.percussion.widgets.image.services;
     
import com.percussion.services.PSBaseServiceLocator;
     

public class ImageCacheManagerLocator extends PSBaseServiceLocator
     {
       private static final String IMAGE_CACHE_BEAN = "imageWidgetCacheManager";
       private static volatile ImageCacheManager rsm=null;
     
       public static ImageCacheManager getImageCacheManager()
       {
           if (rsm==null)
           {
               synchronized (ImageCacheManagerLocator.class)
               {
                   if (rsm==null)
                   {
                       rsm = (ImageCacheManager)getBean(IMAGE_CACHE_BEAN);
                   }
               }
           }
    	   return rsm;
       }
     }

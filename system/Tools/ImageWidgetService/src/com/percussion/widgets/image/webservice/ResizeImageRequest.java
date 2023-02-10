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

package com.percussion.widgets.image.webservice;
      
      public class ResizeImageRequest
      {
        private String imageKey;
        private int height;
        private int width;
        private int x;
        private int y;
        private int deltaX;
        private int deltaY;
        private int rotate;
      
        public ResizeImageRequest()
        {
        	this.height = 0;
        	this.width = 0;
        	this.x = 0;
        	this.y = 0;
        	this.deltaX = 0;
        	this.deltaY = 0;
        	this.rotate = 0;
        }
      
        public String getImageKey()
        {
        	return this.imageKey;
        }
      
        public void setImageKey(String imageKey)
        {
        	this.imageKey = imageKey;
        }
      
        public int getHeight()
        {
        	return this.height;
        }
      
        public void setHeight(int height)
        {
        	this.height = height;
        }
      
        public int getWidth()
        {
        	return this.width;
        }
      
        public void setWidth(int width)
        {
        	this.width = width;
        }
      
        public int getX()
        {
        	return this.x;
        }
      
        public void setX(int x)
        {
        	this.x = x;
        }
      
        public int getY()
        {
        	return this.y;
        }
      
        public void setY(int y)
        {
        	this.y = y;
        }
      
        public int getDeltaX()
        {
        	return this.deltaX;
        }
      
        public void setDeltaX(int deltax)
        {
        	this.deltaX = deltax;
        }
      
        public int getDeltaY()
        {
        	return this.deltaY;
        }
      
        public void setDeltaY(int deltay)
        {
        	this.deltaY = deltay;
        }
      
        public int getRotate()
        {
        	return this.rotate;
        }
      
        public void setRotate(int rotate)
        {
        	this.rotate = rotate;
        }
      }

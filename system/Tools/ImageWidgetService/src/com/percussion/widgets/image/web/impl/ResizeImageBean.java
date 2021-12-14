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

package com.percussion.widgets.image.web.impl;
      
      public class ResizeImageBean
      {
        private String imageKey;
        private int height;
        private int width;
        private int x;
        private int y;
        private int deltaX;
        private int deltaY;
        private int rotate;
      
        public ResizeImageBean()
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

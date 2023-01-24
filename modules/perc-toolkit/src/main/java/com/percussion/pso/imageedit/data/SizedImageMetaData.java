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
package com.percussion.pso.imageedit.data;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A Sized Image. 
 *
 * @author DavidBenua
 *
 */
public class SizedImageMetaData extends SimpleImageMetaData
{
    private ImageSizeDefinition sizeDefinition;

	private int x = 0;
	private int y = 0;
	private Boolean constraint = true;

   public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

/**
    * @return the sizeDefinition
    */
   public ImageSizeDefinition getSizeDefinition()
   {
      return sizeDefinition;
   }

   /**
    * @param sizeDefinition the sizeDefinition to set
    */
   public void setSizeDefinition(ImageSizeDefinition sizeDefinition)
   {
      this.sizeDefinition = sizeDefinition;
   }

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SizedImageMetaData{");
		sb.append("sizeDefinition=").append(sizeDefinition);
		sb.append(", x=").append(x);
		sb.append(", y=").append(y);
		sb.append(", constraint=").append(constraint);
		sb.append('}');
		return sb.toString();
	}

	public Boolean isConstraint()
{
	return constraint;
}

public void setConstraint(Boolean constraint)
{
	this.constraint = constraint;
}

   
}

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

package com.percussion.delivery.integrations.ems.model;

import com.percussion.delivery.integrations.ems.IPSEMSEventService;
import com.percussion.error.PSExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.util.Date;

/**
 * <Data>
    <CategoryID>2147483647</CategoryID>
    <CategoryDescription>Room Charge</CategoryDescription>
    <UseCutOff>false</UseCutOff>
    <CutOffTime>1900-01-01T00:00:00</CutOffTime>
    <CutOffDays>0</CutOffDays>
    <CutOffHours>0</CutOffHours>
    <PONumberRequired>false</PONumberRequired>
    <BillingReferenceRequired>false</BillingReferenceRequired>
    <CurrencySymbol>US Dollars</CurrencySymbol>
    <UseStates>false</UseStates>
    <DefaultStateID>0</DefaultStateID>
  </Data>
 * @author natechadwick
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Category {
	
	private static final Logger log = LogManager.getLogger(Category.class);
	private Integer categoryID;
	private String categoryDesc;
	private boolean useCutOff;
	private Date custoffTime;
	private Integer cutOffDays;
	private Integer cutOffHours;
	private boolean poNumberRequired;
	private boolean billingReferenceRequired;
	private String currencySymbol;
	private boolean useStates;
	private Integer defaultStateId;
	public Integer getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}
	public String getCategoryDesc() {
		return categoryDesc;
	}
	public void setCategoryDesc(String categoryDesc) {
		this.categoryDesc = categoryDesc;
	}
	public boolean isUseCutOff() {
		return useCutOff;
	}
	public void setUseCutOff(boolean useCutOff) {
		this.useCutOff = useCutOff;
	}
	public Date getCustoffTime() {
		return custoffTime;
	}
	public void setCustoffTime(String custoffTime) {
		try {
			this.custoffTime = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(custoffTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting CustoffTime with value {} and format: {}, Error: {}",custoffTime, IPSEMSEventService.TIME_FORMAT_STRING,e.getMessage());
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		}
	}
	public Integer getCutOffDays() {
		return cutOffDays;
	}
	public void setCutOffDays(Integer cutOffDays) {
		this.cutOffDays = cutOffDays;
	}
	public Integer getCutOffHours() {
		return cutOffHours;
	}
	public void setCutOffHours(Integer cutOffHours) {
		this.cutOffHours = cutOffHours;
	}
	public boolean isPoNumberRequired() {
		return poNumberRequired;
	}
	public void setPoNumberRequired(boolean poNumberRequired) {
		this.poNumberRequired = poNumberRequired;
	}
	public boolean isBillingReferenceRequired() {
		return billingReferenceRequired;
	}
	public void setBillingReferenceRequired(boolean billingReferenceRequired) {
		this.billingReferenceRequired = billingReferenceRequired;
	}
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}
	public boolean isUseStates() {
		return useStates;
	}
	public void setUseStates(boolean useStates) {
		this.useStates = useStates;
	}
	public Integer getDefaultStateId() {
		return defaultStateId;
	}
	public void setDefaultStateId(Integer defaultStateId) {
		this.defaultStateId = defaultStateId;
	}
	
	
	
	
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityAttributeFound implements Event{

	private List<FacilityAttribute> facilityAttributes;

	public FacilityAttributeFound(List<FacilityAttribute> facilityAttributes) {
		this.setFacilityAttributes(facilityAttributes);
	}

	public List<FacilityAttribute> getFacilityAttributes()	{
		return facilityAttributes;
	}

	public void setFacilityAttributes(List<FacilityAttribute> facilityAttributes)	{
		this.facilityAttributes = facilityAttributes;
	}
}

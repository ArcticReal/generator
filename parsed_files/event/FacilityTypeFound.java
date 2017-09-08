package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityTypeFound implements Event{

	private List<FacilityType> facilityTypes;

	public FacilityTypeFound(List<FacilityType> facilityTypes) {
		this.setFacilityTypes(facilityTypes);
	}

	public List<FacilityType> getFacilityTypes()	{
		return facilityTypes;
	}

	public void setFacilityTypes(List<FacilityType> facilityTypes)	{
		this.facilityTypes = facilityTypes;
	}
}

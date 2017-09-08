package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityGroupTypeFound implements Event{

	private List<FacilityGroupType> facilityGroupTypes;

	public FacilityGroupTypeFound(List<FacilityGroupType> facilityGroupTypes) {
		this.setFacilityGroupTypes(facilityGroupTypes);
	}

	public List<FacilityGroupType> getFacilityGroupTypes()	{
		return facilityGroupTypes;
	}

	public void setFacilityGroupTypes(List<FacilityGroupType> facilityGroupTypes)	{
		this.facilityGroupTypes = facilityGroupTypes;
	}
}

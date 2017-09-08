package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityGroupFound implements Event{

	private List<FacilityGroup> facilityGroups;

	public FacilityGroupFound(List<FacilityGroup> facilityGroups) {
		this.setFacilityGroups(facilityGroups);
	}

	public List<FacilityGroup> getFacilityGroups()	{
		return facilityGroups;
	}

	public void setFacilityGroups(List<FacilityGroup> facilityGroups)	{
		this.facilityGroups = facilityGroups;
	}
}

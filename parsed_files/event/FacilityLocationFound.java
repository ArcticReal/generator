package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityLocationFound implements Event{

	private List<FacilityLocation> facilityLocations;

	public FacilityLocationFound(List<FacilityLocation> facilityLocations) {
		this.setFacilityLocations(facilityLocations);
	}

	public List<FacilityLocation> getFacilityLocations()	{
		return facilityLocations;
	}

	public void setFacilityLocations(List<FacilityLocation> facilityLocations)	{
		this.facilityLocations = facilityLocations;
	}
}

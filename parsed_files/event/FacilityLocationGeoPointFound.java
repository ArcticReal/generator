package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityLocationGeoPointFound implements Event{

	private List<FacilityLocationGeoPoint> facilityLocationGeoPoints;

	public FacilityLocationGeoPointFound(List<FacilityLocationGeoPoint> facilityLocationGeoPoints) {
		this.setFacilityLocationGeoPoints(facilityLocationGeoPoints);
	}

	public List<FacilityLocationGeoPoint> getFacilityLocationGeoPoints()	{
		return facilityLocationGeoPoints;
	}

	public void setFacilityLocationGeoPoints(List<FacilityLocationGeoPoint> facilityLocationGeoPoints)	{
		this.facilityLocationGeoPoints = facilityLocationGeoPoints;
	}
}

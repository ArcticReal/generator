package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityContentFound implements Event{

	private List<FacilityContent> facilityContents;

	public FacilityContentFound(List<FacilityContent> facilityContents) {
		this.setFacilityContents(facilityContents);
	}

	public List<FacilityContent> getFacilityContents()	{
		return facilityContents;
	}

	public void setFacilityContents(List<FacilityContent> facilityContents)	{
		this.facilityContents = facilityContents;
	}
}

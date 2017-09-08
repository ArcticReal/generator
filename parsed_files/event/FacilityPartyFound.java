package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityPartyFound implements Event{

	private List<FacilityParty> facilityPartys;

	public FacilityPartyFound(List<FacilityParty> facilityPartys) {
		this.setFacilityPartys(facilityPartys);
	}

	public List<FacilityParty> getFacilityPartys()	{
		return facilityPartys;
	}

	public void setFacilityPartys(List<FacilityParty> facilityPartys)	{
		this.facilityPartys = facilityPartys;
	}
}

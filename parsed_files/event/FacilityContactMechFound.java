package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityContactMechFound implements Event{

	private List<FacilityContactMech> facilityContactMechs;

	public FacilityContactMechFound(List<FacilityContactMech> facilityContactMechs) {
		this.setFacilityContactMechs(facilityContactMechs);
	}

	public List<FacilityContactMech> getFacilityContactMechs()	{
		return facilityContactMechs;
	}

	public void setFacilityContactMechs(List<FacilityContactMech> facilityContactMechs)	{
		this.facilityContactMechs = facilityContactMechs;
	}
}

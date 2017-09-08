package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityContactMechPurposeFound implements Event{

	private List<FacilityContactMechPurpose> facilityContactMechPurposes;

	public FacilityContactMechPurposeFound(List<FacilityContactMechPurpose> facilityContactMechPurposes) {
		this.setFacilityContactMechPurposes(facilityContactMechPurposes);
	}

	public List<FacilityContactMechPurpose> getFacilityContactMechPurposes()	{
		return facilityContactMechPurposes;
	}

	public void setFacilityContactMechPurposes(List<FacilityContactMechPurpose> facilityContactMechPurposes)	{
		this.facilityContactMechPurposes = facilityContactMechPurposes;
	}
}

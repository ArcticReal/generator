package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityGroupRollupFound implements Event{

	private List<FacilityGroupRollup> facilityGroupRollups;

	public FacilityGroupRollupFound(List<FacilityGroupRollup> facilityGroupRollups) {
		this.setFacilityGroupRollups(facilityGroupRollups);
	}

	public List<FacilityGroupRollup> getFacilityGroupRollups()	{
		return facilityGroupRollups;
	}

	public void setFacilityGroupRollups(List<FacilityGroupRollup> facilityGroupRollups)	{
		this.facilityGroupRollups = facilityGroupRollups;
	}
}

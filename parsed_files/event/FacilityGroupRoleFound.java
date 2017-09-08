package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityGroupRoleFound implements Event{

	private List<FacilityGroupRole> facilityGroupRoles;

	public FacilityGroupRoleFound(List<FacilityGroupRole> facilityGroupRoles) {
		this.setFacilityGroupRoles(facilityGroupRoles);
	}

	public List<FacilityGroupRole> getFacilityGroupRoles()	{
		return facilityGroupRoles;
	}

	public void setFacilityGroupRoles(List<FacilityGroupRole> facilityGroupRoles)	{
		this.facilityGroupRoles = facilityGroupRoles;
	}
}

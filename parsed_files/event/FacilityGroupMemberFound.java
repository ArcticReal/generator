package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FacilityGroupMemberFound implements Event{

	private List<FacilityGroupMember> facilityGroupMembers;

	public FacilityGroupMemberFound(List<FacilityGroupMember> facilityGroupMembers) {
		this.setFacilityGroupMembers(facilityGroupMembers);
	}

	public List<FacilityGroupMember> getFacilityGroupMembers()	{
		return facilityGroupMembers;
	}

	public void setFacilityGroupMembers(List<FacilityGroupMember> facilityGroupMembers)	{
		this.facilityGroupMembers = facilityGroupMembers;
	}
}

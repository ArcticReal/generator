package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PicklistRoleFound implements Event{

	private List<PicklistRole> picklistRoles;

	public PicklistRoleFound(List<PicklistRole> picklistRoles) {
		this.setPicklistRoles(picklistRoles);
	}

	public List<PicklistRole> getPicklistRoles()	{
		return picklistRoles;
	}

	public void setPicklistRoles(List<PicklistRole> picklistRoles)	{
		this.picklistRoles = picklistRoles;
	}
}

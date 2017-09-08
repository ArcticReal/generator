package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RoleTypeFound implements Event{

	private List<RoleType> roleTypes;

	public RoleTypeFound(List<RoleType> roleTypes) {
		this.setRoleTypes(roleTypes);
	}

	public List<RoleType> getRoleTypes()	{
		return roleTypes;
	}

	public void setRoleTypes(List<RoleType> roleTypes)	{
		this.roleTypes = roleTypes;
	}
}

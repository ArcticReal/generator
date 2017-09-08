package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountRoleFound implements Event{

	private List<FinAccountRole> finAccountRoles;

	public FinAccountRoleFound(List<FinAccountRole> finAccountRoles) {
		this.setFinAccountRoles(finAccountRoles);
	}

	public List<FinAccountRole> getFinAccountRoles()	{
		return finAccountRoles;
	}

	public void setFinAccountRoles(List<FinAccountRole> finAccountRoles)	{
		this.finAccountRoles = finAccountRoles;
	}
}

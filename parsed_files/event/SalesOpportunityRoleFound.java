package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityRoleFound implements Event{

	private List<SalesOpportunityRole> salesOpportunityRoles;

	public SalesOpportunityRoleFound(List<SalesOpportunityRole> salesOpportunityRoles) {
		this.setSalesOpportunityRoles(salesOpportunityRoles);
	}

	public List<SalesOpportunityRole> getSalesOpportunityRoles()	{
		return salesOpportunityRoles;
	}

	public void setSalesOpportunityRoles(List<SalesOpportunityRole> salesOpportunityRoles)	{
		this.salesOpportunityRoles = salesOpportunityRoles;
	}
}

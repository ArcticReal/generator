package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementRoleFound implements Event{

	private List<RequirementRole> requirementRoles;

	public RequirementRoleFound(List<RequirementRole> requirementRoles) {
		this.setRequirementRoles(requirementRoles);
	}

	public List<RequirementRole> getRequirementRoles()	{
		return requirementRoles;
	}

	public void setRequirementRoles(List<RequirementRole> requirementRoles)	{
		this.requirementRoles = requirementRoles;
	}
}

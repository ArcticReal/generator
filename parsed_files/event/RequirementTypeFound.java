package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementTypeFound implements Event{

	private List<RequirementType> requirementTypes;

	public RequirementTypeFound(List<RequirementType> requirementTypes) {
		this.setRequirementTypes(requirementTypes);
	}

	public List<RequirementType> getRequirementTypes()	{
		return requirementTypes;
	}

	public void setRequirementTypes(List<RequirementType> requirementTypes)	{
		this.requirementTypes = requirementTypes;
	}
}

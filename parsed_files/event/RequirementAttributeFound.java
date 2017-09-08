package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementAttributeFound implements Event{

	private List<RequirementAttribute> requirementAttributes;

	public RequirementAttributeFound(List<RequirementAttribute> requirementAttributes) {
		this.setRequirementAttributes(requirementAttributes);
	}

	public List<RequirementAttribute> getRequirementAttributes()	{
		return requirementAttributes;
	}

	public void setRequirementAttributes(List<RequirementAttribute> requirementAttributes)	{
		this.requirementAttributes = requirementAttributes;
	}
}

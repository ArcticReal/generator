package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementFound implements Event{

	private List<Requirement> requirements;

	public RequirementFound(List<Requirement> requirements) {
		this.setRequirements(requirements);
	}

	public List<Requirement> getRequirements()	{
		return requirements;
	}

	public void setRequirements(List<Requirement> requirements)	{
		this.requirements = requirements;
	}
}

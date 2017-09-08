package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementStatusFound implements Event{

	private List<RequirementStatus> requirementStatuss;

	public RequirementStatusFound(List<RequirementStatus> requirementStatuss) {
		this.setRequirementStatuss(requirementStatuss);
	}

	public List<RequirementStatus> getRequirementStatuss()	{
		return requirementStatuss;
	}

	public void setRequirementStatuss(List<RequirementStatus> requirementStatuss)	{
		this.requirementStatuss = requirementStatuss;
	}
}

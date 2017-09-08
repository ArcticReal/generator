package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementBudgetAllocationFound implements Event{

	private List<RequirementBudgetAllocation> requirementBudgetAllocations;

	public RequirementBudgetAllocationFound(List<RequirementBudgetAllocation> requirementBudgetAllocations) {
		this.setRequirementBudgetAllocations(requirementBudgetAllocations);
	}

	public List<RequirementBudgetAllocation> getRequirementBudgetAllocations()	{
		return requirementBudgetAllocations;
	}

	public void setRequirementBudgetAllocations(List<RequirementBudgetAllocation> requirementBudgetAllocations)	{
		this.requirementBudgetAllocations = requirementBudgetAllocations;
	}
}

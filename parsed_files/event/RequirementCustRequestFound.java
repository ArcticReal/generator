package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementCustRequestFound implements Event{

	private List<RequirementCustRequest> requirementCustRequests;

	public RequirementCustRequestFound(List<RequirementCustRequest> requirementCustRequests) {
		this.setRequirementCustRequests(requirementCustRequests);
	}

	public List<RequirementCustRequest> getRequirementCustRequests()	{
		return requirementCustRequests;
	}

	public void setRequirementCustRequests(List<RequirementCustRequest> requirementCustRequests)	{
		this.requirementCustRequests = requirementCustRequests;
	}
}

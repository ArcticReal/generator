package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RequirementTypeAttrFound implements Event{

	private List<RequirementTypeAttr> requirementTypeAttrs;

	public RequirementTypeAttrFound(List<RequirementTypeAttr> requirementTypeAttrs) {
		this.setRequirementTypeAttrs(requirementTypeAttrs);
	}

	public List<RequirementTypeAttr> getRequirementTypeAttrs()	{
		return requirementTypeAttrs;
	}

	public void setRequirementTypeAttrs(List<RequirementTypeAttr> requirementTypeAttrs)	{
		this.requirementTypeAttrs = requirementTypeAttrs;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ResponsibilityTypeFound implements Event{

	private List<ResponsibilityType> responsibilityTypes;

	public ResponsibilityTypeFound(List<ResponsibilityType> responsibilityTypes) {
		this.setResponsibilityTypes(responsibilityTypes);
	}

	public List<ResponsibilityType> getResponsibilityTypes()	{
		return responsibilityTypes;
	}

	public void setResponsibilityTypes(List<ResponsibilityType> responsibilityTypes)	{
		this.responsibilityTypes = responsibilityTypes;
	}
}

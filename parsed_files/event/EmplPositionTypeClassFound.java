package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionTypeClassFound implements Event{

	private List<EmplPositionTypeClass> emplPositionTypeClasss;

	public EmplPositionTypeClassFound(List<EmplPositionTypeClass> emplPositionTypeClasss) {
		this.setEmplPositionTypeClasss(emplPositionTypeClasss);
	}

	public List<EmplPositionTypeClass> getEmplPositionTypeClasss()	{
		return emplPositionTypeClasss;
	}

	public void setEmplPositionTypeClasss(List<EmplPositionTypeClass> emplPositionTypeClasss)	{
		this.emplPositionTypeClasss = emplPositionTypeClasss;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionResponsibilityFound implements Event{

	private List<EmplPositionResponsibility> emplPositionResponsibilitys;

	public EmplPositionResponsibilityFound(List<EmplPositionResponsibility> emplPositionResponsibilitys) {
		this.setEmplPositionResponsibilitys(emplPositionResponsibilitys);
	}

	public List<EmplPositionResponsibility> getEmplPositionResponsibilitys()	{
		return emplPositionResponsibilitys;
	}

	public void setEmplPositionResponsibilitys(List<EmplPositionResponsibility> emplPositionResponsibilitys)	{
		this.emplPositionResponsibilitys = emplPositionResponsibilitys;
	}
}

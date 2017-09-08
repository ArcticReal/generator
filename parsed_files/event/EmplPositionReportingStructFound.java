package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionReportingStructFound implements Event{

	private List<EmplPositionReportingStruct> emplPositionReportingStructs;

	public EmplPositionReportingStructFound(List<EmplPositionReportingStruct> emplPositionReportingStructs) {
		this.setEmplPositionReportingStructs(emplPositionReportingStructs);
	}

	public List<EmplPositionReportingStruct> getEmplPositionReportingStructs()	{
		return emplPositionReportingStructs;
	}

	public void setEmplPositionReportingStructs(List<EmplPositionReportingStruct> emplPositionReportingStructs)	{
		this.emplPositionReportingStructs = emplPositionReportingStructs;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionFound implements Event{

	private List<EmplPosition> emplPositions;

	public EmplPositionFound(List<EmplPosition> emplPositions) {
		this.setEmplPositions(emplPositions);
	}

	public List<EmplPosition> getEmplPositions()	{
		return emplPositions;
	}

	public void setEmplPositions(List<EmplPosition> emplPositions)	{
		this.emplPositions = emplPositions;
	}
}

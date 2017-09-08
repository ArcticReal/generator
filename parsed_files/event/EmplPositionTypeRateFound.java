package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionTypeRateFound implements Event{

	private List<EmplPositionTypeRate> emplPositionTypeRates;

	public EmplPositionTypeRateFound(List<EmplPositionTypeRate> emplPositionTypeRates) {
		this.setEmplPositionTypeRates(emplPositionTypeRates);
	}

	public List<EmplPositionTypeRate> getEmplPositionTypeRates()	{
		return emplPositionTypeRates;
	}

	public void setEmplPositionTypeRates(List<EmplPositionTypeRate> emplPositionTypeRates)	{
		this.emplPositionTypeRates = emplPositionTypeRates;
	}
}

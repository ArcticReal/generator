package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RateTypeFound implements Event{

	private List<RateType> rateTypes;

	public RateTypeFound(List<RateType> rateTypes) {
		this.setRateTypes(rateTypes);
	}

	public List<RateType> getRateTypes()	{
		return rateTypes;
	}

	public void setRateTypes(List<RateType> rateTypes)	{
		this.rateTypes = rateTypes;
	}
}

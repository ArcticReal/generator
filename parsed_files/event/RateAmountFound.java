package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RateAmountFound implements Event{

	private List<RateAmount> rateAmounts;

	public RateAmountFound(List<RateAmount> rateAmounts) {
		this.setRateAmounts(rateAmounts);
	}

	public List<RateAmount> getRateAmounts()	{
		return rateAmounts;
	}

	public void setRateAmounts(List<RateAmount> rateAmounts)	{
		this.rateAmounts = rateAmounts;
	}
}

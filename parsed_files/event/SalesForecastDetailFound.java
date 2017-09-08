package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesForecastDetailFound implements Event{

	private List<SalesForecastDetail> salesForecastDetails;

	public SalesForecastDetailFound(List<SalesForecastDetail> salesForecastDetails) {
		this.setSalesForecastDetails(salesForecastDetails);
	}

	public List<SalesForecastDetail> getSalesForecastDetails()	{
		return salesForecastDetails;
	}

	public void setSalesForecastDetails(List<SalesForecastDetail> salesForecastDetails)	{
		this.salesForecastDetails = salesForecastDetails;
	}
}

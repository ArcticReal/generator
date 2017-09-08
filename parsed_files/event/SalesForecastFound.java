package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesForecastFound implements Event{

	private List<SalesForecast> salesForecasts;

	public SalesForecastFound(List<SalesForecast> salesForecasts) {
		this.setSalesForecasts(salesForecasts);
	}

	public List<SalesForecast> getSalesForecasts()	{
		return salesForecasts;
	}

	public void setSalesForecasts(List<SalesForecast> salesForecasts)	{
		this.salesForecasts = salesForecasts;
	}
}

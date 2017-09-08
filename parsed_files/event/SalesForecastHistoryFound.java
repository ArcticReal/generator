package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesForecastHistoryFound implements Event{

	private List<SalesForecastHistory> salesForecastHistorys;

	public SalesForecastHistoryFound(List<SalesForecastHistory> salesForecastHistorys) {
		this.setSalesForecastHistorys(salesForecastHistorys);
	}

	public List<SalesForecastHistory> getSalesForecastHistorys()	{
		return salesForecastHistorys;
	}

	public void setSalesForecastHistorys(List<SalesForecastHistory> salesForecastHistorys)	{
		this.salesForecastHistorys = salesForecastHistorys;
	}
}

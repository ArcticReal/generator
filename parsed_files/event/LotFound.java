package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class LotFound implements Event{

	private List<Lot> lots;

	public LotFound(List<Lot> lots) {
		this.setLots(lots);
	}

	public List<Lot> getLots()	{
		return lots;
	}

	public void setLots(List<Lot> lots)	{
		this.lots = lots;
	}
}

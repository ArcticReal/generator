package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestCommEventFound implements Event{

	private List<CustRequestCommEvent> custRequestCommEvents;

	public CustRequestCommEventFound(List<CustRequestCommEvent> custRequestCommEvents) {
		this.setCustRequestCommEvents(custRequestCommEvents);
	}

	public List<CustRequestCommEvent> getCustRequestCommEvents()	{
		return custRequestCommEvents;
	}

	public void setCustRequestCommEvents(List<CustRequestCommEvent> custRequestCommEvents)	{
		this.custRequestCommEvents = custRequestCommEvents;
	}
}

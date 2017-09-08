package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestWorkEffortFound implements Event{

	private List<CustRequestWorkEffort> custRequestWorkEfforts;

	public CustRequestWorkEffortFound(List<CustRequestWorkEffort> custRequestWorkEfforts) {
		this.setCustRequestWorkEfforts(custRequestWorkEfforts);
	}

	public List<CustRequestWorkEffort> getCustRequestWorkEfforts()	{
		return custRequestWorkEfforts;
	}

	public void setCustRequestWorkEfforts(List<CustRequestWorkEffort> custRequestWorkEfforts)	{
		this.custRequestWorkEfforts = custRequestWorkEfforts;
	}
}

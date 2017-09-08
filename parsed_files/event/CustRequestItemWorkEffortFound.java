package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestItemWorkEffortFound implements Event{

	private List<CustRequestItemWorkEffort> custRequestItemWorkEfforts;

	public CustRequestItemWorkEffortFound(List<CustRequestItemWorkEffort> custRequestItemWorkEfforts) {
		this.setCustRequestItemWorkEfforts(custRequestItemWorkEfforts);
	}

	public List<CustRequestItemWorkEffort> getCustRequestItemWorkEfforts()	{
		return custRequestItemWorkEfforts;
	}

	public void setCustRequestItemWorkEfforts(List<CustRequestItemWorkEffort> custRequestItemWorkEfforts)	{
		this.custRequestItemWorkEfforts = custRequestItemWorkEfforts;
	}
}

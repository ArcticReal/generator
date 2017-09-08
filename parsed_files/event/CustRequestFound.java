package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestFound implements Event{

	private List<CustRequest> custRequests;

	public CustRequestFound(List<CustRequest> custRequests) {
		this.setCustRequests(custRequests);
	}

	public List<CustRequest> getCustRequests()	{
		return custRequests;
	}

	public void setCustRequests(List<CustRequest> custRequests)	{
		this.custRequests = custRequests;
	}
}

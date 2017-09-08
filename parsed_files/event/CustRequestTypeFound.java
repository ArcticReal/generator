package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestTypeFound implements Event{

	private List<CustRequestType> custRequestTypes;

	public CustRequestTypeFound(List<CustRequestType> custRequestTypes) {
		this.setCustRequestTypes(custRequestTypes);
	}

	public List<CustRequestType> getCustRequestTypes()	{
		return custRequestTypes;
	}

	public void setCustRequestTypes(List<CustRequestType> custRequestTypes)	{
		this.custRequestTypes = custRequestTypes;
	}
}

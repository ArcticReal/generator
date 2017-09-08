package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestStatusFound implements Event{

	private List<CustRequestStatus> custRequestStatuss;

	public CustRequestStatusFound(List<CustRequestStatus> custRequestStatuss) {
		this.setCustRequestStatuss(custRequestStatuss);
	}

	public List<CustRequestStatus> getCustRequestStatuss()	{
		return custRequestStatuss;
	}

	public void setCustRequestStatuss(List<CustRequestStatus> custRequestStatuss)	{
		this.custRequestStatuss = custRequestStatuss;
	}
}

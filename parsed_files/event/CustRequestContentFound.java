package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestContentFound implements Event{

	private List<CustRequestContent> custRequestContents;

	public CustRequestContentFound(List<CustRequestContent> custRequestContents) {
		this.setCustRequestContents(custRequestContents);
	}

	public List<CustRequestContent> getCustRequestContents()	{
		return custRequestContents;
	}

	public void setCustRequestContents(List<CustRequestContent> custRequestContents)	{
		this.custRequestContents = custRequestContents;
	}
}

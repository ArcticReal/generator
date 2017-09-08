package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestItemFound implements Event{

	private List<CustRequestItem> custRequestItems;

	public CustRequestItemFound(List<CustRequestItem> custRequestItems) {
		this.setCustRequestItems(custRequestItems);
	}

	public List<CustRequestItem> getCustRequestItems()	{
		return custRequestItems;
	}

	public void setCustRequestItems(List<CustRequestItem> custRequestItems)	{
		this.custRequestItems = custRequestItems;
	}
}

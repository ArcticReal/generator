package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemBillingFound implements Event{

	private List<ReturnItemBilling> returnItemBillings;

	public ReturnItemBillingFound(List<ReturnItemBilling> returnItemBillings) {
		this.setReturnItemBillings(returnItemBillings);
	}

	public List<ReturnItemBilling> getReturnItemBillings()	{
		return returnItemBillings;
	}

	public void setReturnItemBillings(List<ReturnItemBilling> returnItemBillings)	{
		this.returnItemBillings = returnItemBillings;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BillingAccountFound implements Event{

	private List<BillingAccount> billingAccounts;

	public BillingAccountFound(List<BillingAccount> billingAccounts) {
		this.setBillingAccounts(billingAccounts);
	}

	public List<BillingAccount> getBillingAccounts()	{
		return billingAccounts;
	}

	public void setBillingAccounts(List<BillingAccount> billingAccounts)	{
		this.billingAccounts = billingAccounts;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BillingAccountTermFound implements Event{

	private List<BillingAccountTerm> billingAccountTerms;

	public BillingAccountTermFound(List<BillingAccountTerm> billingAccountTerms) {
		this.setBillingAccountTerms(billingAccountTerms);
	}

	public List<BillingAccountTerm> getBillingAccountTerms()	{
		return billingAccountTerms;
	}

	public void setBillingAccountTerms(List<BillingAccountTerm> billingAccountTerms)	{
		this.billingAccountTerms = billingAccountTerms;
	}
}

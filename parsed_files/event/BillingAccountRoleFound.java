package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BillingAccountRoleFound implements Event{

	private List<BillingAccountRole> billingAccountRoles;

	public BillingAccountRoleFound(List<BillingAccountRole> billingAccountRoles) {
		this.setBillingAccountRoles(billingAccountRoles);
	}

	public List<BillingAccountRole> getBillingAccountRoles()	{
		return billingAccountRoles;
	}

	public void setBillingAccountRoles(List<BillingAccountRole> billingAccountRoles)	{
		this.billingAccountRoles = billingAccountRoles;
	}
}

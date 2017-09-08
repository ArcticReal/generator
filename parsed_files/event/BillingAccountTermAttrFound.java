package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class BillingAccountTermAttrFound implements Event{

	private List<BillingAccountTermAttr> billingAccountTermAttrs;

	public BillingAccountTermAttrFound(List<BillingAccountTermAttr> billingAccountTermAttrs) {
		this.setBillingAccountTermAttrs(billingAccountTermAttrs);
	}

	public List<BillingAccountTermAttr> getBillingAccountTermAttrs()	{
		return billingAccountTermAttrs;
	}

	public void setBillingAccountTermAttrs(List<BillingAccountTermAttr> billingAccountTermAttrs)	{
		this.billingAccountTermAttrs = billingAccountTermAttrs;
	}
}

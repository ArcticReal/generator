package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AffiliateFound implements Event{

	private List<Affiliate> affiliates;

	public AffiliateFound(List<Affiliate> affiliates) {
		this.setAffiliates(affiliates);
	}

	public List<Affiliate> getAffiliates()	{
		return affiliates;
	}

	public void setAffiliates(List<Affiliate> affiliates)	{
		this.affiliates = affiliates;
	}
}

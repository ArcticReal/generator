package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountFound implements Event{

	private List<FinAccount> finAccounts;

	public FinAccountFound(List<FinAccount> finAccounts) {
		this.setFinAccounts(finAccounts);
	}

	public List<FinAccount> getFinAccounts()	{
		return finAccounts;
	}

	public void setFinAccounts(List<FinAccount> finAccounts)	{
		this.finAccounts = finAccounts;
	}
}

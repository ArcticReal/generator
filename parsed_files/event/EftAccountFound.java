package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EftAccountFound implements Event{

	private List<EftAccount> eftAccounts;

	public EftAccountFound(List<EftAccount> eftAccounts) {
		this.setEftAccounts(eftAccounts);
	}

	public List<EftAccount> getEftAccounts()	{
		return eftAccounts;
	}

	public void setEftAccounts(List<EftAccount> eftAccounts)	{
		this.eftAccounts = eftAccounts;
	}
}

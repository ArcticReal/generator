package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTypeGlAccountFound implements Event{

	private List<FinAccountTypeGlAccount> finAccountTypeGlAccounts;

	public FinAccountTypeGlAccountFound(List<FinAccountTypeGlAccount> finAccountTypeGlAccounts) {
		this.setFinAccountTypeGlAccounts(finAccountTypeGlAccounts);
	}

	public List<FinAccountTypeGlAccount> getFinAccountTypeGlAccounts()	{
		return finAccountTypeGlAccounts;
	}

	public void setFinAccountTypeGlAccounts(List<FinAccountTypeGlAccount> finAccountTypeGlAccounts)	{
		this.finAccountTypeGlAccounts = finAccountTypeGlAccounts;
	}
}

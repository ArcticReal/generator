package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CheckAccountFound implements Event{

	private List<CheckAccount> checkAccounts;

	public CheckAccountFound(List<CheckAccount> checkAccounts) {
		this.setCheckAccounts(checkAccounts);
	}

	public List<CheckAccount> getCheckAccounts()	{
		return checkAccounts;
	}

	public void setCheckAccounts(List<CheckAccount> checkAccounts)	{
		this.checkAccounts = checkAccounts;
	}
}

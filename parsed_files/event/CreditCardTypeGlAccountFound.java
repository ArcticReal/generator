package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CreditCardTypeGlAccountFound implements Event{

	private List<CreditCardTypeGlAccount> creditCardTypeGlAccounts;

	public CreditCardTypeGlAccountFound(List<CreditCardTypeGlAccount> creditCardTypeGlAccounts) {
		this.setCreditCardTypeGlAccounts(creditCardTypeGlAccounts);
	}

	public List<CreditCardTypeGlAccount> getCreditCardTypeGlAccounts()	{
		return creditCardTypeGlAccounts;
	}

	public void setCreditCardTypeGlAccounts(List<CreditCardTypeGlAccount> creditCardTypeGlAccounts)	{
		this.creditCardTypeGlAccounts = creditCardTypeGlAccounts;
	}
}

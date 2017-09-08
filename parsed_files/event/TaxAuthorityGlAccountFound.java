package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityGlAccountFound implements Event{

	private List<TaxAuthorityGlAccount> taxAuthorityGlAccounts;

	public TaxAuthorityGlAccountFound(List<TaxAuthorityGlAccount> taxAuthorityGlAccounts) {
		this.setTaxAuthorityGlAccounts(taxAuthorityGlAccounts);
	}

	public List<TaxAuthorityGlAccount> getTaxAuthorityGlAccounts()	{
		return taxAuthorityGlAccounts;
	}

	public void setTaxAuthorityGlAccounts(List<TaxAuthorityGlAccount> taxAuthorityGlAccounts)	{
		this.taxAuthorityGlAccounts = taxAuthorityGlAccounts;
	}
}

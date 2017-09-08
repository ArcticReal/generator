package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityFound implements Event{

	private List<TaxAuthority> taxAuthoritys;

	public TaxAuthorityFound(List<TaxAuthority> taxAuthoritys) {
		this.setTaxAuthoritys(taxAuthoritys);
	}

	public List<TaxAuthority> getTaxAuthoritys()	{
		return taxAuthoritys;
	}

	public void setTaxAuthoritys(List<TaxAuthority> taxAuthoritys)	{
		this.taxAuthoritys = taxAuthoritys;
	}
}

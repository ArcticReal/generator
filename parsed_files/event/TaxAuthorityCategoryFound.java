package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityCategoryFound implements Event{

	private List<TaxAuthorityCategory> taxAuthorityCategorys;

	public TaxAuthorityCategoryFound(List<TaxAuthorityCategory> taxAuthorityCategorys) {
		this.setTaxAuthorityCategorys(taxAuthorityCategorys);
	}

	public List<TaxAuthorityCategory> getTaxAuthorityCategorys()	{
		return taxAuthorityCategorys;
	}

	public void setTaxAuthorityCategorys(List<TaxAuthorityCategory> taxAuthorityCategorys)	{
		this.taxAuthorityCategorys = taxAuthorityCategorys;
	}
}

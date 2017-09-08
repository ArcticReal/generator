package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityAssocFound implements Event{

	private List<TaxAuthorityAssoc> taxAuthorityAssocs;

	public TaxAuthorityAssocFound(List<TaxAuthorityAssoc> taxAuthorityAssocs) {
		this.setTaxAuthorityAssocs(taxAuthorityAssocs);
	}

	public List<TaxAuthorityAssoc> getTaxAuthorityAssocs()	{
		return taxAuthorityAssocs;
	}

	public void setTaxAuthorityAssocs(List<TaxAuthorityAssoc> taxAuthorityAssocs)	{
		this.taxAuthorityAssocs = taxAuthorityAssocs;
	}
}

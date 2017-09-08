package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityAssocTypeFound implements Event{

	private List<TaxAuthorityAssocType> taxAuthorityAssocTypes;

	public TaxAuthorityAssocTypeFound(List<TaxAuthorityAssocType> taxAuthorityAssocTypes) {
		this.setTaxAuthorityAssocTypes(taxAuthorityAssocTypes);
	}

	public List<TaxAuthorityAssocType> getTaxAuthorityAssocTypes()	{
		return taxAuthorityAssocTypes;
	}

	public void setTaxAuthorityAssocTypes(List<TaxAuthorityAssocType> taxAuthorityAssocTypes)	{
		this.taxAuthorityAssocTypes = taxAuthorityAssocTypes;
	}
}

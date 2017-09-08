package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityRateTypeFound implements Event{

	private List<TaxAuthorityRateType> taxAuthorityRateTypes;

	public TaxAuthorityRateTypeFound(List<TaxAuthorityRateType> taxAuthorityRateTypes) {
		this.setTaxAuthorityRateTypes(taxAuthorityRateTypes);
	}

	public List<TaxAuthorityRateType> getTaxAuthorityRateTypes()	{
		return taxAuthorityRateTypes;
	}

	public void setTaxAuthorityRateTypes(List<TaxAuthorityRateType> taxAuthorityRateTypes)	{
		this.taxAuthorityRateTypes = taxAuthorityRateTypes;
	}
}

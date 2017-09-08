package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TaxAuthorityRateProductFound implements Event{

	private List<TaxAuthorityRateProduct> taxAuthorityRateProducts;

	public TaxAuthorityRateProductFound(List<TaxAuthorityRateProduct> taxAuthorityRateProducts) {
		this.setTaxAuthorityRateProducts(taxAuthorityRateProducts);
	}

	public List<TaxAuthorityRateProduct> getTaxAuthorityRateProducts()	{
		return taxAuthorityRateProducts;
	}

	public void setTaxAuthorityRateProducts(List<TaxAuthorityRateProduct> taxAuthorityRateProducts)	{
		this.taxAuthorityRateProducts = taxAuthorityRateProducts;
	}
}

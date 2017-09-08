package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductGlAccountFound implements Event{

	private List<ProductGlAccount> productGlAccounts;

	public ProductGlAccountFound(List<ProductGlAccount> productGlAccounts) {
		this.setProductGlAccounts(productGlAccounts);
	}

	public List<ProductGlAccount> getProductGlAccounts()	{
		return productGlAccounts;
	}

	public void setProductGlAccounts(List<ProductGlAccount> productGlAccounts)	{
		this.productGlAccounts = productGlAccounts;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryGlAccountFound implements Event{

	private List<ProductCategoryGlAccount> productCategoryGlAccounts;

	public ProductCategoryGlAccountFound(List<ProductCategoryGlAccount> productCategoryGlAccounts) {
		this.setProductCategoryGlAccounts(productCategoryGlAccounts);
	}

	public List<ProductCategoryGlAccount> getProductCategoryGlAccounts()	{
		return productCategoryGlAccounts;
	}

	public void setProductCategoryGlAccounts(List<ProductCategoryGlAccount> productCategoryGlAccounts)	{
		this.productCategoryGlAccounts = productCategoryGlAccounts;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryRoleFound implements Event{

	private List<ProductCategoryRole> productCategoryRoles;

	public ProductCategoryRoleFound(List<ProductCategoryRole> productCategoryRoles) {
		this.setProductCategoryRoles(productCategoryRoles);
	}

	public List<ProductCategoryRole> getProductCategoryRoles()	{
		return productCategoryRoles;
	}

	public void setProductCategoryRoles(List<ProductCategoryRole> productCategoryRoles)	{
		this.productCategoryRoles = productCategoryRoles;
	}
}

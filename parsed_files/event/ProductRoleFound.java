package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductRoleFound implements Event{

	private List<ProductRole> productRoles;

	public ProductRoleFound(List<ProductRole> productRoles) {
		this.setProductRoles(productRoles);
	}

	public List<ProductRole> getProductRoles()	{
		return productRoles;
	}

	public void setProductRoles(List<ProductRole> productRoles)	{
		this.productRoles = productRoles;
	}
}

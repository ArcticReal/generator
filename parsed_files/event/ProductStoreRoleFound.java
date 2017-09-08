package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreRoleFound implements Event{

	private List<ProductStoreRole> productStoreRoles;

	public ProductStoreRoleFound(List<ProductStoreRole> productStoreRoles) {
		this.setProductStoreRoles(productStoreRoles);
	}

	public List<ProductStoreRole> getProductStoreRoles()	{
		return productStoreRoles;
	}

	public void setProductStoreRoles(List<ProductStoreRole> productStoreRoles)	{
		this.productStoreRoles = productStoreRoles;
	}
}

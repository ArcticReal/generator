package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreGroupRoleFound implements Event{

	private List<ProductStoreGroupRole> productStoreGroupRoles;

	public ProductStoreGroupRoleFound(List<ProductStoreGroupRole> productStoreGroupRoles) {
		this.setProductStoreGroupRoles(productStoreGroupRoles);
	}

	public List<ProductStoreGroupRole> getProductStoreGroupRoles()	{
		return productStoreGroupRoles;
	}

	public void setProductStoreGroupRoles(List<ProductStoreGroupRole> productStoreGroupRoles)	{
		this.productStoreGroupRoles = productStoreGroupRoles;
	}
}

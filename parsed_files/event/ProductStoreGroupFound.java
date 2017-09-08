package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreGroupFound implements Event{

	private List<ProductStoreGroup> productStoreGroups;

	public ProductStoreGroupFound(List<ProductStoreGroup> productStoreGroups) {
		this.setProductStoreGroups(productStoreGroups);
	}

	public List<ProductStoreGroup> getProductStoreGroups()	{
		return productStoreGroups;
	}

	public void setProductStoreGroups(List<ProductStoreGroup> productStoreGroups)	{
		this.productStoreGroups = productStoreGroups;
	}
}

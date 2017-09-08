package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreGroupRollupFound implements Event{

	private List<ProductStoreGroupRollup> productStoreGroupRollups;

	public ProductStoreGroupRollupFound(List<ProductStoreGroupRollup> productStoreGroupRollups) {
		this.setProductStoreGroupRollups(productStoreGroupRollups);
	}

	public List<ProductStoreGroupRollup> getProductStoreGroupRollups()	{
		return productStoreGroupRollups;
	}

	public void setProductStoreGroupRollups(List<ProductStoreGroupRollup> productStoreGroupRollups)	{
		this.productStoreGroupRollups = productStoreGroupRollups;
	}
}

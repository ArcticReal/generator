package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreGroupTypeFound implements Event{

	private List<ProductStoreGroupType> productStoreGroupTypes;

	public ProductStoreGroupTypeFound(List<ProductStoreGroupType> productStoreGroupTypes) {
		this.setProductStoreGroupTypes(productStoreGroupTypes);
	}

	public List<ProductStoreGroupType> getProductStoreGroupTypes()	{
		return productStoreGroupTypes;
	}

	public void setProductStoreGroupTypes(List<ProductStoreGroupType> productStoreGroupTypes)	{
		this.productStoreGroupTypes = productStoreGroupTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductTypeFound implements Event{

	private List<ProductType> productTypes;

	public ProductTypeFound(List<ProductType> productTypes) {
		this.setProductTypes(productTypes);
	}

	public List<ProductType> getProductTypes()	{
		return productTypes;
	}

	public void setProductTypes(List<ProductType> productTypes)	{
		this.productTypes = productTypes;
	}
}

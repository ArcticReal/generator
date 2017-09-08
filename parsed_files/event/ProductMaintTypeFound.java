package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductMaintTypeFound implements Event{

	private List<ProductMaintType> productMaintTypes;

	public ProductMaintTypeFound(List<ProductMaintType> productMaintTypes) {
		this.setProductMaintTypes(productMaintTypes);
	}

	public List<ProductMaintType> getProductMaintTypes()	{
		return productMaintTypes;
	}

	public void setProductMaintTypes(List<ProductMaintType> productMaintTypes)	{
		this.productMaintTypes = productMaintTypes;
	}
}

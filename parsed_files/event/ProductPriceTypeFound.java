package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceTypeFound implements Event{

	private List<ProductPriceType> productPriceTypes;

	public ProductPriceTypeFound(List<ProductPriceType> productPriceTypes) {
		this.setProductPriceTypes(productPriceTypes);
	}

	public List<ProductPriceType> getProductPriceTypes()	{
		return productPriceTypes;
	}

	public void setProductPriceTypes(List<ProductPriceType> productPriceTypes)	{
		this.productPriceTypes = productPriceTypes;
	}
}

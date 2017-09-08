package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceFound implements Event{

	private List<ProductPrice> productPrices;

	public ProductPriceFound(List<ProductPrice> productPrices) {
		this.setProductPrices(productPrices);
	}

	public List<ProductPrice> getProductPrices()	{
		return productPrices;
	}

	public void setProductPrices(List<ProductPrice> productPrices)	{
		this.productPrices = productPrices;
	}
}

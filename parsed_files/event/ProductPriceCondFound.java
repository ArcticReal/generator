package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceCondFound implements Event{

	private List<ProductPriceCond> productPriceConds;

	public ProductPriceCondFound(List<ProductPriceCond> productPriceConds) {
		this.setProductPriceConds(productPriceConds);
	}

	public List<ProductPriceCond> getProductPriceConds()	{
		return productPriceConds;
	}

	public void setProductPriceConds(List<ProductPriceCond> productPriceConds)	{
		this.productPriceConds = productPriceConds;
	}
}

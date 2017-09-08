package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceChangeFound implements Event{

	private List<ProductPriceChange> productPriceChanges;

	public ProductPriceChangeFound(List<ProductPriceChange> productPriceChanges) {
		this.setProductPriceChanges(productPriceChanges);
	}

	public List<ProductPriceChange> getProductPriceChanges()	{
		return productPriceChanges;
	}

	public void setProductPriceChanges(List<ProductPriceChange> productPriceChanges)	{
		this.productPriceChanges = productPriceChanges;
	}
}

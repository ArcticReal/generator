package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceActionFound implements Event{

	private List<ProductPriceAction> productPriceActions;

	public ProductPriceActionFound(List<ProductPriceAction> productPriceActions) {
		this.setProductPriceActions(productPriceActions);
	}

	public List<ProductPriceAction> getProductPriceActions()	{
		return productPriceActions;
	}

	public void setProductPriceActions(List<ProductPriceAction> productPriceActions)	{
		this.productPriceActions = productPriceActions;
	}
}

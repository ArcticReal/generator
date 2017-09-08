package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoActionFound implements Event{

	private List<ProductPromoAction> productPromoActions;

	public ProductPromoActionFound(List<ProductPromoAction> productPromoActions) {
		this.setProductPromoActions(productPromoActions);
	}

	public List<ProductPromoAction> getProductPromoActions()	{
		return productPromoActions;
	}

	public void setProductPromoActions(List<ProductPromoAction> productPromoActions)	{
		this.productPromoActions = productPromoActions;
	}
}

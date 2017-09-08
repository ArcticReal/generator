package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoUseFound implements Event{

	private List<ProductPromoUse> productPromoUses;

	public ProductPromoUseFound(List<ProductPromoUse> productPromoUses) {
		this.setProductPromoUses(productPromoUses);
	}

	public List<ProductPromoUse> getProductPromoUses()	{
		return productPromoUses;
	}

	public void setProductPromoUses(List<ProductPromoUse> productPromoUses)	{
		this.productPromoUses = productPromoUses;
	}
}

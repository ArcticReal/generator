package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoRuleFound implements Event{

	private List<ProductPromoRule> productPromoRules;

	public ProductPromoRuleFound(List<ProductPromoRule> productPromoRules) {
		this.setProductPromoRules(productPromoRules);
	}

	public List<ProductPromoRule> getProductPromoRules()	{
		return productPromoRules;
	}

	public void setProductPromoRules(List<ProductPromoRule> productPromoRules)	{
		this.productPromoRules = productPromoRules;
	}
}

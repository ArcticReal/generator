package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceRuleFound implements Event{

	private List<ProductPriceRule> productPriceRules;

	public ProductPriceRuleFound(List<ProductPriceRule> productPriceRules) {
		this.setProductPriceRules(productPriceRules);
	}

	public List<ProductPriceRule> getProductPriceRules()	{
		return productPriceRules;
	}

	public void setProductPriceRules(List<ProductPriceRule> productPriceRules)	{
		this.productPriceRules = productPriceRules;
	}
}

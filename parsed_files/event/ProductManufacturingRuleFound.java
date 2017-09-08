package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductManufacturingRuleFound implements Event{

	private List<ProductManufacturingRule> productManufacturingRules;

	public ProductManufacturingRuleFound(List<ProductManufacturingRule> productManufacturingRules) {
		this.setProductManufacturingRules(productManufacturingRules);
	}

	public List<ProductManufacturingRule> getProductManufacturingRules()	{
		return productManufacturingRules;
	}

	public void setProductManufacturingRules(List<ProductManufacturingRule> productManufacturingRules)	{
		this.productManufacturingRules = productManufacturingRules;
	}
}

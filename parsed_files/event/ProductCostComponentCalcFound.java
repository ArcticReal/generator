package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCostComponentCalcFound implements Event{

	private List<ProductCostComponentCalc> productCostComponentCalcs;

	public ProductCostComponentCalcFound(List<ProductCostComponentCalc> productCostComponentCalcs) {
		this.setProductCostComponentCalcs(productCostComponentCalcs);
	}

	public List<ProductCostComponentCalc> getProductCostComponentCalcs()	{
		return productCostComponentCalcs;
	}

	public void setProductCostComponentCalcs(List<ProductCostComponentCalc> productCostComponentCalcs)	{
		this.productCostComponentCalcs = productCostComponentCalcs;
	}
}

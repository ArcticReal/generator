package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceActionTypeFound implements Event{

	private List<ProductPriceActionType> productPriceActionTypes;

	public ProductPriceActionTypeFound(List<ProductPriceActionType> productPriceActionTypes) {
		this.setProductPriceActionTypes(productPriceActionTypes);
	}

	public List<ProductPriceActionType> getProductPriceActionTypes()	{
		return productPriceActionTypes;
	}

	public void setProductPriceActionTypes(List<ProductPriceActionType> productPriceActionTypes)	{
		this.productPriceActionTypes = productPriceActionTypes;
	}
}

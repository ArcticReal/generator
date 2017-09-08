package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryTypeFound implements Event{

	private List<ProductCategoryType> productCategoryTypes;

	public ProductCategoryTypeFound(List<ProductCategoryType> productCategoryTypes) {
		this.setProductCategoryTypes(productCategoryTypes);
	}

	public List<ProductCategoryType> getProductCategoryTypes()	{
		return productCategoryTypes;
	}

	public void setProductCategoryTypes(List<ProductCategoryType> productCategoryTypes)	{
		this.productCategoryTypes = productCategoryTypes;
	}
}

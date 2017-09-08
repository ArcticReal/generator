package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryContentFound implements Event{

	private List<ProductCategoryContent> productCategoryContents;

	public ProductCategoryContentFound(List<ProductCategoryContent> productCategoryContents) {
		this.setProductCategoryContents(productCategoryContents);
	}

	public List<ProductCategoryContent> getProductCategoryContents()	{
		return productCategoryContents;
	}

	public void setProductCategoryContents(List<ProductCategoryContent> productCategoryContents)	{
		this.productCategoryContents = productCategoryContents;
	}
}

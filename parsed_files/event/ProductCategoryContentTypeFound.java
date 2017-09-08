package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryContentTypeFound implements Event{

	private List<ProductCategoryContentType> productCategoryContentTypes;

	public ProductCategoryContentTypeFound(List<ProductCategoryContentType> productCategoryContentTypes) {
		this.setProductCategoryContentTypes(productCategoryContentTypes);
	}

	public List<ProductCategoryContentType> getProductCategoryContentTypes()	{
		return productCategoryContentTypes;
	}

	public void setProductCategoryContentTypes(List<ProductCategoryContentType> productCategoryContentTypes)	{
		this.productCategoryContentTypes = productCategoryContentTypes;
	}
}

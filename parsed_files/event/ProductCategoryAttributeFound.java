package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryAttributeFound implements Event{

	private List<ProductCategoryAttribute> productCategoryAttributes;

	public ProductCategoryAttributeFound(List<ProductCategoryAttribute> productCategoryAttributes) {
		this.setProductCategoryAttributes(productCategoryAttributes);
	}

	public List<ProductCategoryAttribute> getProductCategoryAttributes()	{
		return productCategoryAttributes;
	}

	public void setProductCategoryAttributes(List<ProductCategoryAttribute> productCategoryAttributes)	{
		this.productCategoryAttributes = productCategoryAttributes;
	}
}

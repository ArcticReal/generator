package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductAttributeFound implements Event{

	private List<ProductAttribute> productAttributes;

	public ProductAttributeFound(List<ProductAttribute> productAttributes) {
		this.setProductAttributes(productAttributes);
	}

	public List<ProductAttribute> getProductAttributes()	{
		return productAttributes;
	}

	public void setProductAttributes(List<ProductAttribute> productAttributes)	{
		this.productAttributes = productAttributes;
	}
}

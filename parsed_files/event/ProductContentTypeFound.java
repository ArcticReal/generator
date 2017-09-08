package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductContentTypeFound implements Event{

	private List<ProductContentType> productContentTypes;

	public ProductContentTypeFound(List<ProductContentType> productContentTypes) {
		this.setProductContentTypes(productContentTypes);
	}

	public List<ProductContentType> getProductContentTypes()	{
		return productContentTypes;
	}

	public void setProductContentTypes(List<ProductContentType> productContentTypes)	{
		this.productContentTypes = productContentTypes;
	}
}

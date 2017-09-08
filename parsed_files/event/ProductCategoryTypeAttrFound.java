package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryTypeAttrFound implements Event{

	private List<ProductCategoryTypeAttr> productCategoryTypeAttrs;

	public ProductCategoryTypeAttrFound(List<ProductCategoryTypeAttr> productCategoryTypeAttrs) {
		this.setProductCategoryTypeAttrs(productCategoryTypeAttrs);
	}

	public List<ProductCategoryTypeAttr> getProductCategoryTypeAttrs()	{
		return productCategoryTypeAttrs;
	}

	public void setProductCategoryTypeAttrs(List<ProductCategoryTypeAttr> productCategoryTypeAttrs)	{
		this.productCategoryTypeAttrs = productCategoryTypeAttrs;
	}
}

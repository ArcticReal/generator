package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductTypeAttrFound implements Event{

	private List<ProductTypeAttr> productTypeAttrs;

	public ProductTypeAttrFound(List<ProductTypeAttr> productTypeAttrs) {
		this.setProductTypeAttrs(productTypeAttrs);
	}

	public List<ProductTypeAttr> getProductTypeAttrs()	{
		return productTypeAttrs;
	}

	public void setProductTypeAttrs(List<ProductTypeAttr> productTypeAttrs)	{
		this.productTypeAttrs = productTypeAttrs;
	}
}

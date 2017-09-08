package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryFound implements Event{

	private List<ProductCategory> productCategorys;

	public ProductCategoryFound(List<ProductCategory> productCategorys) {
		this.setProductCategorys(productCategorys);
	}

	public List<ProductCategory> getProductCategorys()	{
		return productCategorys;
	}

	public void setProductCategorys(List<ProductCategory> productCategorys)	{
		this.productCategorys = productCategorys;
	}
}

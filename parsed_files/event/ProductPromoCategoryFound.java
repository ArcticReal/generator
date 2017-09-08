package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoCategoryFound implements Event{

	private List<ProductPromoCategory> productPromoCategorys;

	public ProductPromoCategoryFound(List<ProductPromoCategory> productPromoCategorys) {
		this.setProductPromoCategorys(productPromoCategorys);
	}

	public List<ProductPromoCategory> getProductPromoCategorys()	{
		return productPromoCategorys;
	}

	public void setProductPromoCategorys(List<ProductPromoCategory> productPromoCategorys)	{
		this.productPromoCategorys = productPromoCategorys;
	}
}

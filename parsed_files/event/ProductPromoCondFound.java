package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoCondFound implements Event{

	private List<ProductPromoCond> productPromoConds;

	public ProductPromoCondFound(List<ProductPromoCond> productPromoConds) {
		this.setProductPromoConds(productPromoConds);
	}

	public List<ProductPromoCond> getProductPromoConds()	{
		return productPromoConds;
	}

	public void setProductPromoConds(List<ProductPromoCond> productPromoConds)	{
		this.productPromoConds = productPromoConds;
	}
}

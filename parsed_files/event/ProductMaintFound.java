package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductMaintFound implements Event{

	private List<ProductMaint> productMaints;

	public ProductMaintFound(List<ProductMaint> productMaints) {
		this.setProductMaints(productMaints);
	}

	public List<ProductMaint> getProductMaints()	{
		return productMaints;
	}

	public void setProductMaints(List<ProductMaint> productMaints)	{
		this.productMaints = productMaints;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigStatsFound implements Event{

	private List<ProductConfigStats> productConfigStatss;

	public ProductConfigStatsFound(List<ProductConfigStats> productConfigStatss) {
		this.setProductConfigStatss(productConfigStatss);
	}

	public List<ProductConfigStats> getProductConfigStatss()	{
		return productConfigStatss;
	}

	public void setProductConfigStatss(List<ProductConfigStats> productConfigStatss)	{
		this.productConfigStatss = productConfigStatss;
	}
}

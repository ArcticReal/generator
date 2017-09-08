package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductGeoFound implements Event{

	private List<ProductGeo> productGeos;

	public ProductGeoFound(List<ProductGeo> productGeos) {
		this.setProductGeos(productGeos);
	}

	public List<ProductGeo> getProductGeos()	{
		return productGeos;
	}

	public void setProductGeos(List<ProductGeo> productGeos)	{
		this.productGeos = productGeos;
	}
}

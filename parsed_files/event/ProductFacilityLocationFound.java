package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFacilityLocationFound implements Event{

	private List<ProductFacilityLocation> productFacilityLocations;

	public ProductFacilityLocationFound(List<ProductFacilityLocation> productFacilityLocations) {
		this.setProductFacilityLocations(productFacilityLocations);
	}

	public List<ProductFacilityLocation> getProductFacilityLocations()	{
		return productFacilityLocations;
	}

	public void setProductFacilityLocations(List<ProductFacilityLocation> productFacilityLocations)	{
		this.productFacilityLocations = productFacilityLocations;
	}
}

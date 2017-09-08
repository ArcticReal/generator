package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreShipmentMethFound implements Event{

	private List<ProductStoreShipmentMeth> productStoreShipmentMeths;

	public ProductStoreShipmentMethFound(List<ProductStoreShipmentMeth> productStoreShipmentMeths) {
		this.setProductStoreShipmentMeths(productStoreShipmentMeths);
	}

	public List<ProductStoreShipmentMeth> getProductStoreShipmentMeths()	{
		return productStoreShipmentMeths;
	}

	public void setProductStoreShipmentMeths(List<ProductStoreShipmentMeth> productStoreShipmentMeths)	{
		this.productStoreShipmentMeths = productStoreShipmentMeths;
	}
}

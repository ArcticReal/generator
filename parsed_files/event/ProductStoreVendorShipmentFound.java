package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreVendorShipmentFound implements Event{

	private List<ProductStoreVendorShipment> productStoreVendorShipments;

	public ProductStoreVendorShipmentFound(List<ProductStoreVendorShipment> productStoreVendorShipments) {
		this.setProductStoreVendorShipments(productStoreVendorShipments);
	}

	public List<ProductStoreVendorShipment> getProductStoreVendorShipments()	{
		return productStoreVendorShipments;
	}

	public void setProductStoreVendorShipments(List<ProductStoreVendorShipment> productStoreVendorShipments)	{
		this.productStoreVendorShipments = productStoreVendorShipments;
	}
}

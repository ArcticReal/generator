package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PhysicalInventoryFound implements Event{

	private List<PhysicalInventory> physicalInventorys;

	public PhysicalInventoryFound(List<PhysicalInventory> physicalInventorys) {
		this.setPhysicalInventorys(physicalInventorys);
	}

	public List<PhysicalInventory> getPhysicalInventorys()	{
		return physicalInventorys;
	}

	public void setPhysicalInventorys(List<PhysicalInventory> physicalInventorys)	{
		this.physicalInventorys = physicalInventorys;
	}
}

package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddVendor;
import com.skytala.eCommerce.command.DeleteVendor;
import com.skytala.eCommerce.command.UpdateVendor;
import com.skytala.eCommerce.entity.Vendor;
import com.skytala.eCommerce.entity.VendorMapper;
import com.skytala.eCommerce.event.VendorAdded;
import com.skytala.eCommerce.event.VendorDeleted;
import com.skytala.eCommerce.event.VendorFound;
import com.skytala.eCommerce.event.VendorUpdated;
import com.skytala.eCommerce.query.FindVendorsBy;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Vendor>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public VendorController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Vendor
	 * @return a List with the Vendors
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Vendor> findVendorsBy(@RequestParam Map<String, String> allRequestParams) {

		FindVendorsBy query = new FindVendorsBy(allRequestParams);

		int usedTicketId;

		synchronized (VendorController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorFound.class,
				event -> sendVendorsFoundMessage(((VendorFound) event).getVendors(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendVendorsFoundMessage(List<Vendor> vendors, int usedTicketId) {
		queryReturnVal.put(usedTicketId, vendors);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createVendor(HttpServletRequest request) {

		Vendor vendorToBeAdded = new Vendor();
		try {
			vendorToBeAdded = VendorMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createVendor(vendorToBeAdded);

	}

	/**
	 * creates a new Vendor entry in the ofbiz database
	 * 
	 * @param vendorToBeAdded
	 *            the Vendor thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createVendor(Vendor vendorToBeAdded) {

		AddVendor com = new AddVendor(vendorToBeAdded);
		int usedTicketId;

		synchronized (VendorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorAdded.class,
				event -> sendVendorChangedMessage(((VendorAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateVendor(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		Vendor vendorToBeUpdated = new Vendor();

		try {
			vendorToBeUpdated = VendorMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateVendor(vendorToBeUpdated);

	}

	/**
	 * Updates the Vendor with the specific Id
	 * 
	 * @param vendorToBeUpdated the Vendor thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateVendor(Vendor vendorToBeUpdated) {

		UpdateVendor com = new UpdateVendor(vendorToBeUpdated);

		int usedTicketId;

		synchronized (VendorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorUpdated.class,
				event -> sendVendorChangedMessage(((VendorUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a Vendor from the database
	 * 
	 * @param vendorId:
	 *            the id of the Vendor thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevendorById(@RequestParam(value = "vendorId") String vendorId) {

		DeleteVendor com = new DeleteVendor(vendorId);

		int usedTicketId;

		synchronized (VendorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorDeleted.class,
				event -> sendVendorChangedMessage(((VendorDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendVendorChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/vendor/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}

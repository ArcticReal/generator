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
import com.skytala.eCommerce.command.AddVendorProduct;
import com.skytala.eCommerce.command.DeleteVendorProduct;
import com.skytala.eCommerce.command.UpdateVendorProduct;
import com.skytala.eCommerce.entity.VendorProduct;
import com.skytala.eCommerce.entity.VendorProductMapper;
import com.skytala.eCommerce.event.VendorProductAdded;
import com.skytala.eCommerce.event.VendorProductDeleted;
import com.skytala.eCommerce.event.VendorProductFound;
import com.skytala.eCommerce.event.VendorProductUpdated;
import com.skytala.eCommerce.query.FindVendorProductsBy;

@RestController
@RequestMapping("/api/vendorProduct")
public class VendorProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<VendorProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public VendorProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a VendorProduct
	 * @return a List with the VendorProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<VendorProduct> findVendorProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindVendorProductsBy query = new FindVendorProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (VendorProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorProductFound.class,
				event -> sendVendorProductsFoundMessage(((VendorProductFound) event).getVendorProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendVendorProductsFoundMessage(List<VendorProduct> vendorProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, vendorProducts);
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
	public boolean createVendorProduct(HttpServletRequest request) {

		VendorProduct vendorProductToBeAdded = new VendorProduct();
		try {
			vendorProductToBeAdded = VendorProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createVendorProduct(vendorProductToBeAdded);

	}

	/**
	 * creates a new VendorProduct entry in the ofbiz database
	 * 
	 * @param vendorProductToBeAdded
	 *            the VendorProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createVendorProduct(VendorProduct vendorProductToBeAdded) {

		AddVendorProduct com = new AddVendorProduct(vendorProductToBeAdded);
		int usedTicketId;

		synchronized (VendorProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorProductAdded.class,
				event -> sendVendorProductChangedMessage(((VendorProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateVendorProduct(HttpServletRequest request) {

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

		VendorProduct vendorProductToBeUpdated = new VendorProduct();

		try {
			vendorProductToBeUpdated = VendorProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateVendorProduct(vendorProductToBeUpdated);

	}

	/**
	 * Updates the VendorProduct with the specific Id
	 * 
	 * @param vendorProductToBeUpdated the VendorProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateVendorProduct(VendorProduct vendorProductToBeUpdated) {

		UpdateVendorProduct com = new UpdateVendorProduct(vendorProductToBeUpdated);

		int usedTicketId;

		synchronized (VendorProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorProductUpdated.class,
				event -> sendVendorProductChangedMessage(((VendorProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a VendorProduct from the database
	 * 
	 * @param vendorProductId:
	 *            the id of the VendorProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevendorProductById(@RequestParam(value = "vendorProductId") String vendorProductId) {

		DeleteVendorProduct com = new DeleteVendorProduct(vendorProductId);

		int usedTicketId;

		synchronized (VendorProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VendorProductDeleted.class,
				event -> sendVendorProductChangedMessage(((VendorProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendVendorProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/vendorProduct/\" plus one of the following: "
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

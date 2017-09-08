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
import com.skytala.eCommerce.command.AddSupplierProduct;
import com.skytala.eCommerce.command.DeleteSupplierProduct;
import com.skytala.eCommerce.command.UpdateSupplierProduct;
import com.skytala.eCommerce.entity.SupplierProduct;
import com.skytala.eCommerce.entity.SupplierProductMapper;
import com.skytala.eCommerce.event.SupplierProductAdded;
import com.skytala.eCommerce.event.SupplierProductDeleted;
import com.skytala.eCommerce.event.SupplierProductFound;
import com.skytala.eCommerce.event.SupplierProductUpdated;
import com.skytala.eCommerce.query.FindSupplierProductsBy;

@RestController
@RequestMapping("/api/supplierProduct")
public class SupplierProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SupplierProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SupplierProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SupplierProduct
	 * @return a List with the SupplierProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SupplierProduct> findSupplierProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSupplierProductsBy query = new FindSupplierProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (SupplierProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductFound.class,
				event -> sendSupplierProductsFoundMessage(((SupplierProductFound) event).getSupplierProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSupplierProductsFoundMessage(List<SupplierProduct> supplierProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, supplierProducts);
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
	public boolean createSupplierProduct(HttpServletRequest request) {

		SupplierProduct supplierProductToBeAdded = new SupplierProduct();
		try {
			supplierProductToBeAdded = SupplierProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSupplierProduct(supplierProductToBeAdded);

	}

	/**
	 * creates a new SupplierProduct entry in the ofbiz database
	 * 
	 * @param supplierProductToBeAdded
	 *            the SupplierProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSupplierProduct(SupplierProduct supplierProductToBeAdded) {

		AddSupplierProduct com = new AddSupplierProduct(supplierProductToBeAdded);
		int usedTicketId;

		synchronized (SupplierProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductAdded.class,
				event -> sendSupplierProductChangedMessage(((SupplierProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSupplierProduct(HttpServletRequest request) {

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

		SupplierProduct supplierProductToBeUpdated = new SupplierProduct();

		try {
			supplierProductToBeUpdated = SupplierProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSupplierProduct(supplierProductToBeUpdated);

	}

	/**
	 * Updates the SupplierProduct with the specific Id
	 * 
	 * @param supplierProductToBeUpdated the SupplierProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSupplierProduct(SupplierProduct supplierProductToBeUpdated) {

		UpdateSupplierProduct com = new UpdateSupplierProduct(supplierProductToBeUpdated);

		int usedTicketId;

		synchronized (SupplierProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductUpdated.class,
				event -> sendSupplierProductChangedMessage(((SupplierProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SupplierProduct from the database
	 * 
	 * @param supplierProductId:
	 *            the id of the SupplierProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesupplierProductById(@RequestParam(value = "supplierProductId") String supplierProductId) {

		DeleteSupplierProduct com = new DeleteSupplierProduct(supplierProductId);

		int usedTicketId;

		synchronized (SupplierProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductDeleted.class,
				event -> sendSupplierProductChangedMessage(((SupplierProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSupplierProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/supplierProduct/\" plus one of the following: "
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

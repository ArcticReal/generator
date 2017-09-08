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
import com.skytala.eCommerce.command.AddSupplierPrefOrder;
import com.skytala.eCommerce.command.DeleteSupplierPrefOrder;
import com.skytala.eCommerce.command.UpdateSupplierPrefOrder;
import com.skytala.eCommerce.entity.SupplierPrefOrder;
import com.skytala.eCommerce.entity.SupplierPrefOrderMapper;
import com.skytala.eCommerce.event.SupplierPrefOrderAdded;
import com.skytala.eCommerce.event.SupplierPrefOrderDeleted;
import com.skytala.eCommerce.event.SupplierPrefOrderFound;
import com.skytala.eCommerce.event.SupplierPrefOrderUpdated;
import com.skytala.eCommerce.query.FindSupplierPrefOrdersBy;

@RestController
@RequestMapping("/api/supplierPrefOrder")
public class SupplierPrefOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SupplierPrefOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SupplierPrefOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SupplierPrefOrder
	 * @return a List with the SupplierPrefOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SupplierPrefOrder> findSupplierPrefOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindSupplierPrefOrdersBy query = new FindSupplierPrefOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (SupplierPrefOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierPrefOrderFound.class,
				event -> sendSupplierPrefOrdersFoundMessage(((SupplierPrefOrderFound) event).getSupplierPrefOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSupplierPrefOrdersFoundMessage(List<SupplierPrefOrder> supplierPrefOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, supplierPrefOrders);
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
	public boolean createSupplierPrefOrder(HttpServletRequest request) {

		SupplierPrefOrder supplierPrefOrderToBeAdded = new SupplierPrefOrder();
		try {
			supplierPrefOrderToBeAdded = SupplierPrefOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSupplierPrefOrder(supplierPrefOrderToBeAdded);

	}

	/**
	 * creates a new SupplierPrefOrder entry in the ofbiz database
	 * 
	 * @param supplierPrefOrderToBeAdded
	 *            the SupplierPrefOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSupplierPrefOrder(SupplierPrefOrder supplierPrefOrderToBeAdded) {

		AddSupplierPrefOrder com = new AddSupplierPrefOrder(supplierPrefOrderToBeAdded);
		int usedTicketId;

		synchronized (SupplierPrefOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierPrefOrderAdded.class,
				event -> sendSupplierPrefOrderChangedMessage(((SupplierPrefOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSupplierPrefOrder(HttpServletRequest request) {

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

		SupplierPrefOrder supplierPrefOrderToBeUpdated = new SupplierPrefOrder();

		try {
			supplierPrefOrderToBeUpdated = SupplierPrefOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSupplierPrefOrder(supplierPrefOrderToBeUpdated);

	}

	/**
	 * Updates the SupplierPrefOrder with the specific Id
	 * 
	 * @param supplierPrefOrderToBeUpdated the SupplierPrefOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSupplierPrefOrder(SupplierPrefOrder supplierPrefOrderToBeUpdated) {

		UpdateSupplierPrefOrder com = new UpdateSupplierPrefOrder(supplierPrefOrderToBeUpdated);

		int usedTicketId;

		synchronized (SupplierPrefOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierPrefOrderUpdated.class,
				event -> sendSupplierPrefOrderChangedMessage(((SupplierPrefOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SupplierPrefOrder from the database
	 * 
	 * @param supplierPrefOrderId:
	 *            the id of the SupplierPrefOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesupplierPrefOrderById(@RequestParam(value = "supplierPrefOrderId") String supplierPrefOrderId) {

		DeleteSupplierPrefOrder com = new DeleteSupplierPrefOrder(supplierPrefOrderId);

		int usedTicketId;

		synchronized (SupplierPrefOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierPrefOrderDeleted.class,
				event -> sendSupplierPrefOrderChangedMessage(((SupplierPrefOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSupplierPrefOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/supplierPrefOrder/\" plus one of the following: "
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

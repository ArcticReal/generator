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
import com.skytala.eCommerce.command.AddFixedAssetMaintOrder;
import com.skytala.eCommerce.command.DeleteFixedAssetMaintOrder;
import com.skytala.eCommerce.command.UpdateFixedAssetMaintOrder;
import com.skytala.eCommerce.entity.FixedAssetMaintOrder;
import com.skytala.eCommerce.entity.FixedAssetMaintOrderMapper;
import com.skytala.eCommerce.event.FixedAssetMaintOrderAdded;
import com.skytala.eCommerce.event.FixedAssetMaintOrderDeleted;
import com.skytala.eCommerce.event.FixedAssetMaintOrderFound;
import com.skytala.eCommerce.event.FixedAssetMaintOrderUpdated;
import com.skytala.eCommerce.query.FindFixedAssetMaintOrdersBy;

@RestController
@RequestMapping("/api/fixedAssetMaintOrder")
public class FixedAssetMaintOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetMaintOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetMaintOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetMaintOrder
	 * @return a List with the FixedAssetMaintOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetMaintOrder> findFixedAssetMaintOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetMaintOrdersBy query = new FindFixedAssetMaintOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetMaintOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintOrderFound.class,
				event -> sendFixedAssetMaintOrdersFoundMessage(((FixedAssetMaintOrderFound) event).getFixedAssetMaintOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetMaintOrdersFoundMessage(List<FixedAssetMaintOrder> fixedAssetMaintOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetMaintOrders);
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
	public boolean createFixedAssetMaintOrder(HttpServletRequest request) {

		FixedAssetMaintOrder fixedAssetMaintOrderToBeAdded = new FixedAssetMaintOrder();
		try {
			fixedAssetMaintOrderToBeAdded = FixedAssetMaintOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetMaintOrder(fixedAssetMaintOrderToBeAdded);

	}

	/**
	 * creates a new FixedAssetMaintOrder entry in the ofbiz database
	 * 
	 * @param fixedAssetMaintOrderToBeAdded
	 *            the FixedAssetMaintOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetMaintOrder(FixedAssetMaintOrder fixedAssetMaintOrderToBeAdded) {

		AddFixedAssetMaintOrder com = new AddFixedAssetMaintOrder(fixedAssetMaintOrderToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetMaintOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintOrderAdded.class,
				event -> sendFixedAssetMaintOrderChangedMessage(((FixedAssetMaintOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetMaintOrder(HttpServletRequest request) {

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

		FixedAssetMaintOrder fixedAssetMaintOrderToBeUpdated = new FixedAssetMaintOrder();

		try {
			fixedAssetMaintOrderToBeUpdated = FixedAssetMaintOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetMaintOrder(fixedAssetMaintOrderToBeUpdated);

	}

	/**
	 * Updates the FixedAssetMaintOrder with the specific Id
	 * 
	 * @param fixedAssetMaintOrderToBeUpdated the FixedAssetMaintOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetMaintOrder(FixedAssetMaintOrder fixedAssetMaintOrderToBeUpdated) {

		UpdateFixedAssetMaintOrder com = new UpdateFixedAssetMaintOrder(fixedAssetMaintOrderToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetMaintOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintOrderUpdated.class,
				event -> sendFixedAssetMaintOrderChangedMessage(((FixedAssetMaintOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetMaintOrder from the database
	 * 
	 * @param fixedAssetMaintOrderId:
	 *            the id of the FixedAssetMaintOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetMaintOrderById(@RequestParam(value = "fixedAssetMaintOrderId") String fixedAssetMaintOrderId) {

		DeleteFixedAssetMaintOrder com = new DeleteFixedAssetMaintOrder(fixedAssetMaintOrderId);

		int usedTicketId;

		synchronized (FixedAssetMaintOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintOrderDeleted.class,
				event -> sendFixedAssetMaintOrderChangedMessage(((FixedAssetMaintOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetMaintOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetMaintOrder/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddQuantityBreakType;
import com.skytala.eCommerce.command.DeleteQuantityBreakType;
import com.skytala.eCommerce.command.UpdateQuantityBreakType;
import com.skytala.eCommerce.entity.QuantityBreakType;
import com.skytala.eCommerce.entity.QuantityBreakTypeMapper;
import com.skytala.eCommerce.event.QuantityBreakTypeAdded;
import com.skytala.eCommerce.event.QuantityBreakTypeDeleted;
import com.skytala.eCommerce.event.QuantityBreakTypeFound;
import com.skytala.eCommerce.event.QuantityBreakTypeUpdated;
import com.skytala.eCommerce.query.FindQuantityBreakTypesBy;

@RestController
@RequestMapping("/api/quantityBreakType")
public class QuantityBreakTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuantityBreakType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuantityBreakTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuantityBreakType
	 * @return a List with the QuantityBreakTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuantityBreakType> findQuantityBreakTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuantityBreakTypesBy query = new FindQuantityBreakTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuantityBreakTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuantityBreakTypeFound.class,
				event -> sendQuantityBreakTypesFoundMessage(((QuantityBreakTypeFound) event).getQuantityBreakTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuantityBreakTypesFoundMessage(List<QuantityBreakType> quantityBreakTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quantityBreakTypes);
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
	public boolean createQuantityBreakType(HttpServletRequest request) {

		QuantityBreakType quantityBreakTypeToBeAdded = new QuantityBreakType();
		try {
			quantityBreakTypeToBeAdded = QuantityBreakTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuantityBreakType(quantityBreakTypeToBeAdded);

	}

	/**
	 * creates a new QuantityBreakType entry in the ofbiz database
	 * 
	 * @param quantityBreakTypeToBeAdded
	 *            the QuantityBreakType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuantityBreakType(QuantityBreakType quantityBreakTypeToBeAdded) {

		AddQuantityBreakType com = new AddQuantityBreakType(quantityBreakTypeToBeAdded);
		int usedTicketId;

		synchronized (QuantityBreakTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuantityBreakTypeAdded.class,
				event -> sendQuantityBreakTypeChangedMessage(((QuantityBreakTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuantityBreakType(HttpServletRequest request) {

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

		QuantityBreakType quantityBreakTypeToBeUpdated = new QuantityBreakType();

		try {
			quantityBreakTypeToBeUpdated = QuantityBreakTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuantityBreakType(quantityBreakTypeToBeUpdated);

	}

	/**
	 * Updates the QuantityBreakType with the specific Id
	 * 
	 * @param quantityBreakTypeToBeUpdated the QuantityBreakType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuantityBreakType(QuantityBreakType quantityBreakTypeToBeUpdated) {

		UpdateQuantityBreakType com = new UpdateQuantityBreakType(quantityBreakTypeToBeUpdated);

		int usedTicketId;

		synchronized (QuantityBreakTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuantityBreakTypeUpdated.class,
				event -> sendQuantityBreakTypeChangedMessage(((QuantityBreakTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuantityBreakType from the database
	 * 
	 * @param quantityBreakTypeId:
	 *            the id of the QuantityBreakType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequantityBreakTypeById(@RequestParam(value = "quantityBreakTypeId") String quantityBreakTypeId) {

		DeleteQuantityBreakType com = new DeleteQuantityBreakType(quantityBreakTypeId);

		int usedTicketId;

		synchronized (QuantityBreakTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuantityBreakTypeDeleted.class,
				event -> sendQuantityBreakTypeChangedMessage(((QuantityBreakTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuantityBreakTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quantityBreakType/\" plus one of the following: "
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

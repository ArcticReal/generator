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
import com.skytala.eCommerce.command.AddCostComponentType;
import com.skytala.eCommerce.command.DeleteCostComponentType;
import com.skytala.eCommerce.command.UpdateCostComponentType;
import com.skytala.eCommerce.entity.CostComponentType;
import com.skytala.eCommerce.entity.CostComponentTypeMapper;
import com.skytala.eCommerce.event.CostComponentTypeAdded;
import com.skytala.eCommerce.event.CostComponentTypeDeleted;
import com.skytala.eCommerce.event.CostComponentTypeFound;
import com.skytala.eCommerce.event.CostComponentTypeUpdated;
import com.skytala.eCommerce.query.FindCostComponentTypesBy;

@RestController
@RequestMapping("/api/costComponentType")
public class CostComponentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CostComponentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CostComponentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CostComponentType
	 * @return a List with the CostComponentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CostComponentType> findCostComponentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCostComponentTypesBy query = new FindCostComponentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (CostComponentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeFound.class,
				event -> sendCostComponentTypesFoundMessage(((CostComponentTypeFound) event).getCostComponentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCostComponentTypesFoundMessage(List<CostComponentType> costComponentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, costComponentTypes);
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
	public boolean createCostComponentType(HttpServletRequest request) {

		CostComponentType costComponentTypeToBeAdded = new CostComponentType();
		try {
			costComponentTypeToBeAdded = CostComponentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCostComponentType(costComponentTypeToBeAdded);

	}

	/**
	 * creates a new CostComponentType entry in the ofbiz database
	 * 
	 * @param costComponentTypeToBeAdded
	 *            the CostComponentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCostComponentType(CostComponentType costComponentTypeToBeAdded) {

		AddCostComponentType com = new AddCostComponentType(costComponentTypeToBeAdded);
		int usedTicketId;

		synchronized (CostComponentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeAdded.class,
				event -> sendCostComponentTypeChangedMessage(((CostComponentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCostComponentType(HttpServletRequest request) {

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

		CostComponentType costComponentTypeToBeUpdated = new CostComponentType();

		try {
			costComponentTypeToBeUpdated = CostComponentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCostComponentType(costComponentTypeToBeUpdated);

	}

	/**
	 * Updates the CostComponentType with the specific Id
	 * 
	 * @param costComponentTypeToBeUpdated the CostComponentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCostComponentType(CostComponentType costComponentTypeToBeUpdated) {

		UpdateCostComponentType com = new UpdateCostComponentType(costComponentTypeToBeUpdated);

		int usedTicketId;

		synchronized (CostComponentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeUpdated.class,
				event -> sendCostComponentTypeChangedMessage(((CostComponentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CostComponentType from the database
	 * 
	 * @param costComponentTypeId:
	 *            the id of the CostComponentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecostComponentTypeById(@RequestParam(value = "costComponentTypeId") String costComponentTypeId) {

		DeleteCostComponentType com = new DeleteCostComponentType(costComponentTypeId);

		int usedTicketId;

		synchronized (CostComponentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeDeleted.class,
				event -> sendCostComponentTypeChangedMessage(((CostComponentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCostComponentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/costComponentType/\" plus one of the following: "
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

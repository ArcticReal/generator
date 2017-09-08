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
import com.skytala.eCommerce.command.AddCostComponentAttribute;
import com.skytala.eCommerce.command.DeleteCostComponentAttribute;
import com.skytala.eCommerce.command.UpdateCostComponentAttribute;
import com.skytala.eCommerce.entity.CostComponentAttribute;
import com.skytala.eCommerce.entity.CostComponentAttributeMapper;
import com.skytala.eCommerce.event.CostComponentAttributeAdded;
import com.skytala.eCommerce.event.CostComponentAttributeDeleted;
import com.skytala.eCommerce.event.CostComponentAttributeFound;
import com.skytala.eCommerce.event.CostComponentAttributeUpdated;
import com.skytala.eCommerce.query.FindCostComponentAttributesBy;

@RestController
@RequestMapping("/api/costComponentAttribute")
public class CostComponentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CostComponentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CostComponentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CostComponentAttribute
	 * @return a List with the CostComponentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CostComponentAttribute> findCostComponentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCostComponentAttributesBy query = new FindCostComponentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (CostComponentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentAttributeFound.class,
				event -> sendCostComponentAttributesFoundMessage(((CostComponentAttributeFound) event).getCostComponentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCostComponentAttributesFoundMessage(List<CostComponentAttribute> costComponentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, costComponentAttributes);
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
	public boolean createCostComponentAttribute(HttpServletRequest request) {

		CostComponentAttribute costComponentAttributeToBeAdded = new CostComponentAttribute();
		try {
			costComponentAttributeToBeAdded = CostComponentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCostComponentAttribute(costComponentAttributeToBeAdded);

	}

	/**
	 * creates a new CostComponentAttribute entry in the ofbiz database
	 * 
	 * @param costComponentAttributeToBeAdded
	 *            the CostComponentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCostComponentAttribute(CostComponentAttribute costComponentAttributeToBeAdded) {

		AddCostComponentAttribute com = new AddCostComponentAttribute(costComponentAttributeToBeAdded);
		int usedTicketId;

		synchronized (CostComponentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentAttributeAdded.class,
				event -> sendCostComponentAttributeChangedMessage(((CostComponentAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCostComponentAttribute(HttpServletRequest request) {

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

		CostComponentAttribute costComponentAttributeToBeUpdated = new CostComponentAttribute();

		try {
			costComponentAttributeToBeUpdated = CostComponentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCostComponentAttribute(costComponentAttributeToBeUpdated);

	}

	/**
	 * Updates the CostComponentAttribute with the specific Id
	 * 
	 * @param costComponentAttributeToBeUpdated the CostComponentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCostComponentAttribute(CostComponentAttribute costComponentAttributeToBeUpdated) {

		UpdateCostComponentAttribute com = new UpdateCostComponentAttribute(costComponentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (CostComponentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentAttributeUpdated.class,
				event -> sendCostComponentAttributeChangedMessage(((CostComponentAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CostComponentAttribute from the database
	 * 
	 * @param costComponentAttributeId:
	 *            the id of the CostComponentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecostComponentAttributeById(@RequestParam(value = "costComponentAttributeId") String costComponentAttributeId) {

		DeleteCostComponentAttribute com = new DeleteCostComponentAttribute(costComponentAttributeId);

		int usedTicketId;

		synchronized (CostComponentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentAttributeDeleted.class,
				event -> sendCostComponentAttributeChangedMessage(((CostComponentAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCostComponentAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/costComponentAttribute/\" plus one of the following: "
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

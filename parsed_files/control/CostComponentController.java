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
import com.skytala.eCommerce.command.AddCostComponent;
import com.skytala.eCommerce.command.DeleteCostComponent;
import com.skytala.eCommerce.command.UpdateCostComponent;
import com.skytala.eCommerce.entity.CostComponent;
import com.skytala.eCommerce.entity.CostComponentMapper;
import com.skytala.eCommerce.event.CostComponentAdded;
import com.skytala.eCommerce.event.CostComponentDeleted;
import com.skytala.eCommerce.event.CostComponentFound;
import com.skytala.eCommerce.event.CostComponentUpdated;
import com.skytala.eCommerce.query.FindCostComponentsBy;

@RestController
@RequestMapping("/api/costComponent")
public class CostComponentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CostComponent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CostComponentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CostComponent
	 * @return a List with the CostComponents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CostComponent> findCostComponentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCostComponentsBy query = new FindCostComponentsBy(allRequestParams);

		int usedTicketId;

		synchronized (CostComponentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentFound.class,
				event -> sendCostComponentsFoundMessage(((CostComponentFound) event).getCostComponents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCostComponentsFoundMessage(List<CostComponent> costComponents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, costComponents);
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
	public boolean createCostComponent(HttpServletRequest request) {

		CostComponent costComponentToBeAdded = new CostComponent();
		try {
			costComponentToBeAdded = CostComponentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCostComponent(costComponentToBeAdded);

	}

	/**
	 * creates a new CostComponent entry in the ofbiz database
	 * 
	 * @param costComponentToBeAdded
	 *            the CostComponent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCostComponent(CostComponent costComponentToBeAdded) {

		AddCostComponent com = new AddCostComponent(costComponentToBeAdded);
		int usedTicketId;

		synchronized (CostComponentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentAdded.class,
				event -> sendCostComponentChangedMessage(((CostComponentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCostComponent(HttpServletRequest request) {

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

		CostComponent costComponentToBeUpdated = new CostComponent();

		try {
			costComponentToBeUpdated = CostComponentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCostComponent(costComponentToBeUpdated);

	}

	/**
	 * Updates the CostComponent with the specific Id
	 * 
	 * @param costComponentToBeUpdated the CostComponent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCostComponent(CostComponent costComponentToBeUpdated) {

		UpdateCostComponent com = new UpdateCostComponent(costComponentToBeUpdated);

		int usedTicketId;

		synchronized (CostComponentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentUpdated.class,
				event -> sendCostComponentChangedMessage(((CostComponentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CostComponent from the database
	 * 
	 * @param costComponentId:
	 *            the id of the CostComponent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecostComponentById(@RequestParam(value = "costComponentId") String costComponentId) {

		DeleteCostComponent com = new DeleteCostComponent(costComponentId);

		int usedTicketId;

		synchronized (CostComponentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentDeleted.class,
				event -> sendCostComponentChangedMessage(((CostComponentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCostComponentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/costComponent/\" plus one of the following: "
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

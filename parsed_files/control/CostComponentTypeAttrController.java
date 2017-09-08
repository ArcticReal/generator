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
import com.skytala.eCommerce.command.AddCostComponentTypeAttr;
import com.skytala.eCommerce.command.DeleteCostComponentTypeAttr;
import com.skytala.eCommerce.command.UpdateCostComponentTypeAttr;
import com.skytala.eCommerce.entity.CostComponentTypeAttr;
import com.skytala.eCommerce.entity.CostComponentTypeAttrMapper;
import com.skytala.eCommerce.event.CostComponentTypeAttrAdded;
import com.skytala.eCommerce.event.CostComponentTypeAttrDeleted;
import com.skytala.eCommerce.event.CostComponentTypeAttrFound;
import com.skytala.eCommerce.event.CostComponentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindCostComponentTypeAttrsBy;

@RestController
@RequestMapping("/api/costComponentTypeAttr")
public class CostComponentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CostComponentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CostComponentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CostComponentTypeAttr
	 * @return a List with the CostComponentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CostComponentTypeAttr> findCostComponentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCostComponentTypeAttrsBy query = new FindCostComponentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (CostComponentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeAttrFound.class,
				event -> sendCostComponentTypeAttrsFoundMessage(((CostComponentTypeAttrFound) event).getCostComponentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCostComponentTypeAttrsFoundMessage(List<CostComponentTypeAttr> costComponentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, costComponentTypeAttrs);
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
	public boolean createCostComponentTypeAttr(HttpServletRequest request) {

		CostComponentTypeAttr costComponentTypeAttrToBeAdded = new CostComponentTypeAttr();
		try {
			costComponentTypeAttrToBeAdded = CostComponentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCostComponentTypeAttr(costComponentTypeAttrToBeAdded);

	}

	/**
	 * creates a new CostComponentTypeAttr entry in the ofbiz database
	 * 
	 * @param costComponentTypeAttrToBeAdded
	 *            the CostComponentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCostComponentTypeAttr(CostComponentTypeAttr costComponentTypeAttrToBeAdded) {

		AddCostComponentTypeAttr com = new AddCostComponentTypeAttr(costComponentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (CostComponentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeAttrAdded.class,
				event -> sendCostComponentTypeAttrChangedMessage(((CostComponentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCostComponentTypeAttr(HttpServletRequest request) {

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

		CostComponentTypeAttr costComponentTypeAttrToBeUpdated = new CostComponentTypeAttr();

		try {
			costComponentTypeAttrToBeUpdated = CostComponentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCostComponentTypeAttr(costComponentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the CostComponentTypeAttr with the specific Id
	 * 
	 * @param costComponentTypeAttrToBeUpdated the CostComponentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCostComponentTypeAttr(CostComponentTypeAttr costComponentTypeAttrToBeUpdated) {

		UpdateCostComponentTypeAttr com = new UpdateCostComponentTypeAttr(costComponentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (CostComponentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeAttrUpdated.class,
				event -> sendCostComponentTypeAttrChangedMessage(((CostComponentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CostComponentTypeAttr from the database
	 * 
	 * @param costComponentTypeAttrId:
	 *            the id of the CostComponentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecostComponentTypeAttrById(@RequestParam(value = "costComponentTypeAttrId") String costComponentTypeAttrId) {

		DeleteCostComponentTypeAttr com = new DeleteCostComponentTypeAttr(costComponentTypeAttrId);

		int usedTicketId;

		synchronized (CostComponentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentTypeAttrDeleted.class,
				event -> sendCostComponentTypeAttrChangedMessage(((CostComponentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCostComponentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/costComponentTypeAttr/\" plus one of the following: "
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

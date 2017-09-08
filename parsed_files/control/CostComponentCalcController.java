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
import com.skytala.eCommerce.command.AddCostComponentCalc;
import com.skytala.eCommerce.command.DeleteCostComponentCalc;
import com.skytala.eCommerce.command.UpdateCostComponentCalc;
import com.skytala.eCommerce.entity.CostComponentCalc;
import com.skytala.eCommerce.entity.CostComponentCalcMapper;
import com.skytala.eCommerce.event.CostComponentCalcAdded;
import com.skytala.eCommerce.event.CostComponentCalcDeleted;
import com.skytala.eCommerce.event.CostComponentCalcFound;
import com.skytala.eCommerce.event.CostComponentCalcUpdated;
import com.skytala.eCommerce.query.FindCostComponentCalcsBy;

@RestController
@RequestMapping("/api/costComponentCalc")
public class CostComponentCalcController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CostComponentCalc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CostComponentCalcController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CostComponentCalc
	 * @return a List with the CostComponentCalcs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CostComponentCalc> findCostComponentCalcsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCostComponentCalcsBy query = new FindCostComponentCalcsBy(allRequestParams);

		int usedTicketId;

		synchronized (CostComponentCalcController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentCalcFound.class,
				event -> sendCostComponentCalcsFoundMessage(((CostComponentCalcFound) event).getCostComponentCalcs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCostComponentCalcsFoundMessage(List<CostComponentCalc> costComponentCalcs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, costComponentCalcs);
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
	public boolean createCostComponentCalc(HttpServletRequest request) {

		CostComponentCalc costComponentCalcToBeAdded = new CostComponentCalc();
		try {
			costComponentCalcToBeAdded = CostComponentCalcMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCostComponentCalc(costComponentCalcToBeAdded);

	}

	/**
	 * creates a new CostComponentCalc entry in the ofbiz database
	 * 
	 * @param costComponentCalcToBeAdded
	 *            the CostComponentCalc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCostComponentCalc(CostComponentCalc costComponentCalcToBeAdded) {

		AddCostComponentCalc com = new AddCostComponentCalc(costComponentCalcToBeAdded);
		int usedTicketId;

		synchronized (CostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentCalcAdded.class,
				event -> sendCostComponentCalcChangedMessage(((CostComponentCalcAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCostComponentCalc(HttpServletRequest request) {

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

		CostComponentCalc costComponentCalcToBeUpdated = new CostComponentCalc();

		try {
			costComponentCalcToBeUpdated = CostComponentCalcMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCostComponentCalc(costComponentCalcToBeUpdated);

	}

	/**
	 * Updates the CostComponentCalc with the specific Id
	 * 
	 * @param costComponentCalcToBeUpdated the CostComponentCalc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCostComponentCalc(CostComponentCalc costComponentCalcToBeUpdated) {

		UpdateCostComponentCalc com = new UpdateCostComponentCalc(costComponentCalcToBeUpdated);

		int usedTicketId;

		synchronized (CostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentCalcUpdated.class,
				event -> sendCostComponentCalcChangedMessage(((CostComponentCalcUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CostComponentCalc from the database
	 * 
	 * @param costComponentCalcId:
	 *            the id of the CostComponentCalc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecostComponentCalcById(@RequestParam(value = "costComponentCalcId") String costComponentCalcId) {

		DeleteCostComponentCalc com = new DeleteCostComponentCalc(costComponentCalcId);

		int usedTicketId;

		synchronized (CostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CostComponentCalcDeleted.class,
				event -> sendCostComponentCalcChangedMessage(((CostComponentCalcDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCostComponentCalcChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/costComponentCalc/\" plus one of the following: "
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

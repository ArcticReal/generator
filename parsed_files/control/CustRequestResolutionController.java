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
import com.skytala.eCommerce.command.AddCustRequestResolution;
import com.skytala.eCommerce.command.DeleteCustRequestResolution;
import com.skytala.eCommerce.command.UpdateCustRequestResolution;
import com.skytala.eCommerce.entity.CustRequestResolution;
import com.skytala.eCommerce.entity.CustRequestResolutionMapper;
import com.skytala.eCommerce.event.CustRequestResolutionAdded;
import com.skytala.eCommerce.event.CustRequestResolutionDeleted;
import com.skytala.eCommerce.event.CustRequestResolutionFound;
import com.skytala.eCommerce.event.CustRequestResolutionUpdated;
import com.skytala.eCommerce.query.FindCustRequestResolutionsBy;

@RestController
@RequestMapping("/api/custRequestResolution")
public class CustRequestResolutionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestResolution>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestResolutionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestResolution
	 * @return a List with the CustRequestResolutions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestResolution> findCustRequestResolutionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestResolutionsBy query = new FindCustRequestResolutionsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestResolutionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestResolutionFound.class,
				event -> sendCustRequestResolutionsFoundMessage(((CustRequestResolutionFound) event).getCustRequestResolutions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestResolutionsFoundMessage(List<CustRequestResolution> custRequestResolutions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestResolutions);
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
	public boolean createCustRequestResolution(HttpServletRequest request) {

		CustRequestResolution custRequestResolutionToBeAdded = new CustRequestResolution();
		try {
			custRequestResolutionToBeAdded = CustRequestResolutionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestResolution(custRequestResolutionToBeAdded);

	}

	/**
	 * creates a new CustRequestResolution entry in the ofbiz database
	 * 
	 * @param custRequestResolutionToBeAdded
	 *            the CustRequestResolution thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestResolution(CustRequestResolution custRequestResolutionToBeAdded) {

		AddCustRequestResolution com = new AddCustRequestResolution(custRequestResolutionToBeAdded);
		int usedTicketId;

		synchronized (CustRequestResolutionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestResolutionAdded.class,
				event -> sendCustRequestResolutionChangedMessage(((CustRequestResolutionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestResolution(HttpServletRequest request) {

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

		CustRequestResolution custRequestResolutionToBeUpdated = new CustRequestResolution();

		try {
			custRequestResolutionToBeUpdated = CustRequestResolutionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestResolution(custRequestResolutionToBeUpdated);

	}

	/**
	 * Updates the CustRequestResolution with the specific Id
	 * 
	 * @param custRequestResolutionToBeUpdated the CustRequestResolution thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestResolution(CustRequestResolution custRequestResolutionToBeUpdated) {

		UpdateCustRequestResolution com = new UpdateCustRequestResolution(custRequestResolutionToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestResolutionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestResolutionUpdated.class,
				event -> sendCustRequestResolutionChangedMessage(((CustRequestResolutionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestResolution from the database
	 * 
	 * @param custRequestResolutionId:
	 *            the id of the CustRequestResolution thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestResolutionById(@RequestParam(value = "custRequestResolutionId") String custRequestResolutionId) {

		DeleteCustRequestResolution com = new DeleteCustRequestResolution(custRequestResolutionId);

		int usedTicketId;

		synchronized (CustRequestResolutionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestResolutionDeleted.class,
				event -> sendCustRequestResolutionChangedMessage(((CustRequestResolutionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestResolutionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestResolution/\" plus one of the following: "
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

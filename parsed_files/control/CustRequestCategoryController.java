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
import com.skytala.eCommerce.command.AddCustRequestCategory;
import com.skytala.eCommerce.command.DeleteCustRequestCategory;
import com.skytala.eCommerce.command.UpdateCustRequestCategory;
import com.skytala.eCommerce.entity.CustRequestCategory;
import com.skytala.eCommerce.entity.CustRequestCategoryMapper;
import com.skytala.eCommerce.event.CustRequestCategoryAdded;
import com.skytala.eCommerce.event.CustRequestCategoryDeleted;
import com.skytala.eCommerce.event.CustRequestCategoryFound;
import com.skytala.eCommerce.event.CustRequestCategoryUpdated;
import com.skytala.eCommerce.query.FindCustRequestCategorysBy;

@RestController
@RequestMapping("/api/custRequestCategory")
public class CustRequestCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestCategory
	 * @return a List with the CustRequestCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestCategory> findCustRequestCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestCategorysBy query = new FindCustRequestCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCategoryFound.class,
				event -> sendCustRequestCategorysFoundMessage(((CustRequestCategoryFound) event).getCustRequestCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestCategorysFoundMessage(List<CustRequestCategory> custRequestCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestCategorys);
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
	public boolean createCustRequestCategory(HttpServletRequest request) {

		CustRequestCategory custRequestCategoryToBeAdded = new CustRequestCategory();
		try {
			custRequestCategoryToBeAdded = CustRequestCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestCategory(custRequestCategoryToBeAdded);

	}

	/**
	 * creates a new CustRequestCategory entry in the ofbiz database
	 * 
	 * @param custRequestCategoryToBeAdded
	 *            the CustRequestCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestCategory(CustRequestCategory custRequestCategoryToBeAdded) {

		AddCustRequestCategory com = new AddCustRequestCategory(custRequestCategoryToBeAdded);
		int usedTicketId;

		synchronized (CustRequestCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCategoryAdded.class,
				event -> sendCustRequestCategoryChangedMessage(((CustRequestCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestCategory(HttpServletRequest request) {

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

		CustRequestCategory custRequestCategoryToBeUpdated = new CustRequestCategory();

		try {
			custRequestCategoryToBeUpdated = CustRequestCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestCategory(custRequestCategoryToBeUpdated);

	}

	/**
	 * Updates the CustRequestCategory with the specific Id
	 * 
	 * @param custRequestCategoryToBeUpdated the CustRequestCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestCategory(CustRequestCategory custRequestCategoryToBeUpdated) {

		UpdateCustRequestCategory com = new UpdateCustRequestCategory(custRequestCategoryToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCategoryUpdated.class,
				event -> sendCustRequestCategoryChangedMessage(((CustRequestCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestCategory from the database
	 * 
	 * @param custRequestCategoryId:
	 *            the id of the CustRequestCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestCategoryById(@RequestParam(value = "custRequestCategoryId") String custRequestCategoryId) {

		DeleteCustRequestCategory com = new DeleteCustRequestCategory(custRequestCategoryId);

		int usedTicketId;

		synchronized (CustRequestCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCategoryDeleted.class,
				event -> sendCustRequestCategoryChangedMessage(((CustRequestCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestCategory/\" plus one of the following: "
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

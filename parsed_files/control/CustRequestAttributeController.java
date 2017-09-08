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
import com.skytala.eCommerce.command.AddCustRequestAttribute;
import com.skytala.eCommerce.command.DeleteCustRequestAttribute;
import com.skytala.eCommerce.command.UpdateCustRequestAttribute;
import com.skytala.eCommerce.entity.CustRequestAttribute;
import com.skytala.eCommerce.entity.CustRequestAttributeMapper;
import com.skytala.eCommerce.event.CustRequestAttributeAdded;
import com.skytala.eCommerce.event.CustRequestAttributeDeleted;
import com.skytala.eCommerce.event.CustRequestAttributeFound;
import com.skytala.eCommerce.event.CustRequestAttributeUpdated;
import com.skytala.eCommerce.query.FindCustRequestAttributesBy;

@RestController
@RequestMapping("/api/custRequestAttribute")
public class CustRequestAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestAttribute
	 * @return a List with the CustRequestAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestAttribute> findCustRequestAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestAttributesBy query = new FindCustRequestAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestAttributeFound.class,
				event -> sendCustRequestAttributesFoundMessage(((CustRequestAttributeFound) event).getCustRequestAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestAttributesFoundMessage(List<CustRequestAttribute> custRequestAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestAttributes);
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
	public boolean createCustRequestAttribute(HttpServletRequest request) {

		CustRequestAttribute custRequestAttributeToBeAdded = new CustRequestAttribute();
		try {
			custRequestAttributeToBeAdded = CustRequestAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestAttribute(custRequestAttributeToBeAdded);

	}

	/**
	 * creates a new CustRequestAttribute entry in the ofbiz database
	 * 
	 * @param custRequestAttributeToBeAdded
	 *            the CustRequestAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestAttribute(CustRequestAttribute custRequestAttributeToBeAdded) {

		AddCustRequestAttribute com = new AddCustRequestAttribute(custRequestAttributeToBeAdded);
		int usedTicketId;

		synchronized (CustRequestAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestAttributeAdded.class,
				event -> sendCustRequestAttributeChangedMessage(((CustRequestAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestAttribute(HttpServletRequest request) {

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

		CustRequestAttribute custRequestAttributeToBeUpdated = new CustRequestAttribute();

		try {
			custRequestAttributeToBeUpdated = CustRequestAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestAttribute(custRequestAttributeToBeUpdated);

	}

	/**
	 * Updates the CustRequestAttribute with the specific Id
	 * 
	 * @param custRequestAttributeToBeUpdated the CustRequestAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestAttribute(CustRequestAttribute custRequestAttributeToBeUpdated) {

		UpdateCustRequestAttribute com = new UpdateCustRequestAttribute(custRequestAttributeToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestAttributeUpdated.class,
				event -> sendCustRequestAttributeChangedMessage(((CustRequestAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestAttribute from the database
	 * 
	 * @param custRequestAttributeId:
	 *            the id of the CustRequestAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestAttributeById(@RequestParam(value = "custRequestAttributeId") String custRequestAttributeId) {

		DeleteCustRequestAttribute com = new DeleteCustRequestAttribute(custRequestAttributeId);

		int usedTicketId;

		synchronized (CustRequestAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestAttributeDeleted.class,
				event -> sendCustRequestAttributeChangedMessage(((CustRequestAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestAttribute/\" plus one of the following: "
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

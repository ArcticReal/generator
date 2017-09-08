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
import com.skytala.eCommerce.command.AddAcctgTransAttribute;
import com.skytala.eCommerce.command.DeleteAcctgTransAttribute;
import com.skytala.eCommerce.command.UpdateAcctgTransAttribute;
import com.skytala.eCommerce.entity.AcctgTransAttribute;
import com.skytala.eCommerce.entity.AcctgTransAttributeMapper;
import com.skytala.eCommerce.event.AcctgTransAttributeAdded;
import com.skytala.eCommerce.event.AcctgTransAttributeDeleted;
import com.skytala.eCommerce.event.AcctgTransAttributeFound;
import com.skytala.eCommerce.event.AcctgTransAttributeUpdated;
import com.skytala.eCommerce.query.FindAcctgTransAttributesBy;

@RestController
@RequestMapping("/api/acctgTransAttribute")
public class AcctgTransAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AcctgTransAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AcctgTransAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AcctgTransAttribute
	 * @return a List with the AcctgTransAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AcctgTransAttribute> findAcctgTransAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAcctgTransAttributesBy query = new FindAcctgTransAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (AcctgTransAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransAttributeFound.class,
				event -> sendAcctgTransAttributesFoundMessage(((AcctgTransAttributeFound) event).getAcctgTransAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAcctgTransAttributesFoundMessage(List<AcctgTransAttribute> acctgTransAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, acctgTransAttributes);
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
	public boolean createAcctgTransAttribute(HttpServletRequest request) {

		AcctgTransAttribute acctgTransAttributeToBeAdded = new AcctgTransAttribute();
		try {
			acctgTransAttributeToBeAdded = AcctgTransAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAcctgTransAttribute(acctgTransAttributeToBeAdded);

	}

	/**
	 * creates a new AcctgTransAttribute entry in the ofbiz database
	 * 
	 * @param acctgTransAttributeToBeAdded
	 *            the AcctgTransAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAcctgTransAttribute(AcctgTransAttribute acctgTransAttributeToBeAdded) {

		AddAcctgTransAttribute com = new AddAcctgTransAttribute(acctgTransAttributeToBeAdded);
		int usedTicketId;

		synchronized (AcctgTransAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransAttributeAdded.class,
				event -> sendAcctgTransAttributeChangedMessage(((AcctgTransAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAcctgTransAttribute(HttpServletRequest request) {

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

		AcctgTransAttribute acctgTransAttributeToBeUpdated = new AcctgTransAttribute();

		try {
			acctgTransAttributeToBeUpdated = AcctgTransAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAcctgTransAttribute(acctgTransAttributeToBeUpdated);

	}

	/**
	 * Updates the AcctgTransAttribute with the specific Id
	 * 
	 * @param acctgTransAttributeToBeUpdated the AcctgTransAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAcctgTransAttribute(AcctgTransAttribute acctgTransAttributeToBeUpdated) {

		UpdateAcctgTransAttribute com = new UpdateAcctgTransAttribute(acctgTransAttributeToBeUpdated);

		int usedTicketId;

		synchronized (AcctgTransAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransAttributeUpdated.class,
				event -> sendAcctgTransAttributeChangedMessage(((AcctgTransAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AcctgTransAttribute from the database
	 * 
	 * @param acctgTransAttributeId:
	 *            the id of the AcctgTransAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteacctgTransAttributeById(@RequestParam(value = "acctgTransAttributeId") String acctgTransAttributeId) {

		DeleteAcctgTransAttribute com = new DeleteAcctgTransAttribute(acctgTransAttributeId);

		int usedTicketId;

		synchronized (AcctgTransAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransAttributeDeleted.class,
				event -> sendAcctgTransAttributeChangedMessage(((AcctgTransAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAcctgTransAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/acctgTransAttribute/\" plus one of the following: "
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

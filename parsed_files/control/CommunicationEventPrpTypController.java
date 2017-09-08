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
import com.skytala.eCommerce.command.AddCommunicationEventPrpTyp;
import com.skytala.eCommerce.command.DeleteCommunicationEventPrpTyp;
import com.skytala.eCommerce.command.UpdateCommunicationEventPrpTyp;
import com.skytala.eCommerce.entity.CommunicationEventPrpTyp;
import com.skytala.eCommerce.entity.CommunicationEventPrpTypMapper;
import com.skytala.eCommerce.event.CommunicationEventPrpTypAdded;
import com.skytala.eCommerce.event.CommunicationEventPrpTypDeleted;
import com.skytala.eCommerce.event.CommunicationEventPrpTypFound;
import com.skytala.eCommerce.event.CommunicationEventPrpTypUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventPrpTypsBy;

@RestController
@RequestMapping("/api/communicationEventPrpTyp")
public class CommunicationEventPrpTypController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventPrpTyp>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventPrpTypController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventPrpTyp
	 * @return a List with the CommunicationEventPrpTyps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventPrpTyp> findCommunicationEventPrpTypsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventPrpTypsBy query = new FindCommunicationEventPrpTypsBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventPrpTypController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPrpTypFound.class,
				event -> sendCommunicationEventPrpTypsFoundMessage(((CommunicationEventPrpTypFound) event).getCommunicationEventPrpTyps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventPrpTypsFoundMessage(List<CommunicationEventPrpTyp> communicationEventPrpTyps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventPrpTyps);
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
	public boolean createCommunicationEventPrpTyp(HttpServletRequest request) {

		CommunicationEventPrpTyp communicationEventPrpTypToBeAdded = new CommunicationEventPrpTyp();
		try {
			communicationEventPrpTypToBeAdded = CommunicationEventPrpTypMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventPrpTyp(communicationEventPrpTypToBeAdded);

	}

	/**
	 * creates a new CommunicationEventPrpTyp entry in the ofbiz database
	 * 
	 * @param communicationEventPrpTypToBeAdded
	 *            the CommunicationEventPrpTyp thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventPrpTyp(CommunicationEventPrpTyp communicationEventPrpTypToBeAdded) {

		AddCommunicationEventPrpTyp com = new AddCommunicationEventPrpTyp(communicationEventPrpTypToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventPrpTypController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPrpTypAdded.class,
				event -> sendCommunicationEventPrpTypChangedMessage(((CommunicationEventPrpTypAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventPrpTyp(HttpServletRequest request) {

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

		CommunicationEventPrpTyp communicationEventPrpTypToBeUpdated = new CommunicationEventPrpTyp();

		try {
			communicationEventPrpTypToBeUpdated = CommunicationEventPrpTypMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventPrpTyp(communicationEventPrpTypToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventPrpTyp with the specific Id
	 * 
	 * @param communicationEventPrpTypToBeUpdated the CommunicationEventPrpTyp thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventPrpTyp(CommunicationEventPrpTyp communicationEventPrpTypToBeUpdated) {

		UpdateCommunicationEventPrpTyp com = new UpdateCommunicationEventPrpTyp(communicationEventPrpTypToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventPrpTypController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPrpTypUpdated.class,
				event -> sendCommunicationEventPrpTypChangedMessage(((CommunicationEventPrpTypUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventPrpTyp from the database
	 * 
	 * @param communicationEventPrpTypId:
	 *            the id of the CommunicationEventPrpTyp thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventPrpTypById(@RequestParam(value = "communicationEventPrpTypId") String communicationEventPrpTypId) {

		DeleteCommunicationEventPrpTyp com = new DeleteCommunicationEventPrpTyp(communicationEventPrpTypId);

		int usedTicketId;

		synchronized (CommunicationEventPrpTypController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPrpTypDeleted.class,
				event -> sendCommunicationEventPrpTypChangedMessage(((CommunicationEventPrpTypDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventPrpTypChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventPrpTyp/\" plus one of the following: "
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

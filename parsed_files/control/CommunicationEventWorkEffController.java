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
import com.skytala.eCommerce.command.AddCommunicationEventWorkEff;
import com.skytala.eCommerce.command.DeleteCommunicationEventWorkEff;
import com.skytala.eCommerce.command.UpdateCommunicationEventWorkEff;
import com.skytala.eCommerce.entity.CommunicationEventWorkEff;
import com.skytala.eCommerce.entity.CommunicationEventWorkEffMapper;
import com.skytala.eCommerce.event.CommunicationEventWorkEffAdded;
import com.skytala.eCommerce.event.CommunicationEventWorkEffDeleted;
import com.skytala.eCommerce.event.CommunicationEventWorkEffFound;
import com.skytala.eCommerce.event.CommunicationEventWorkEffUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventWorkEffsBy;

@RestController
@RequestMapping("/api/communicationEventWorkEff")
public class CommunicationEventWorkEffController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventWorkEff>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventWorkEffController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventWorkEff
	 * @return a List with the CommunicationEventWorkEffs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventWorkEff> findCommunicationEventWorkEffsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventWorkEffsBy query = new FindCommunicationEventWorkEffsBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventWorkEffController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventWorkEffFound.class,
				event -> sendCommunicationEventWorkEffsFoundMessage(((CommunicationEventWorkEffFound) event).getCommunicationEventWorkEffs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventWorkEffsFoundMessage(List<CommunicationEventWorkEff> communicationEventWorkEffs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventWorkEffs);
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
	public boolean createCommunicationEventWorkEff(HttpServletRequest request) {

		CommunicationEventWorkEff communicationEventWorkEffToBeAdded = new CommunicationEventWorkEff();
		try {
			communicationEventWorkEffToBeAdded = CommunicationEventWorkEffMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventWorkEff(communicationEventWorkEffToBeAdded);

	}

	/**
	 * creates a new CommunicationEventWorkEff entry in the ofbiz database
	 * 
	 * @param communicationEventWorkEffToBeAdded
	 *            the CommunicationEventWorkEff thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventWorkEff(CommunicationEventWorkEff communicationEventWorkEffToBeAdded) {

		AddCommunicationEventWorkEff com = new AddCommunicationEventWorkEff(communicationEventWorkEffToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventWorkEffController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventWorkEffAdded.class,
				event -> sendCommunicationEventWorkEffChangedMessage(((CommunicationEventWorkEffAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventWorkEff(HttpServletRequest request) {

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

		CommunicationEventWorkEff communicationEventWorkEffToBeUpdated = new CommunicationEventWorkEff();

		try {
			communicationEventWorkEffToBeUpdated = CommunicationEventWorkEffMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventWorkEff(communicationEventWorkEffToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventWorkEff with the specific Id
	 * 
	 * @param communicationEventWorkEffToBeUpdated the CommunicationEventWorkEff thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventWorkEff(CommunicationEventWorkEff communicationEventWorkEffToBeUpdated) {

		UpdateCommunicationEventWorkEff com = new UpdateCommunicationEventWorkEff(communicationEventWorkEffToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventWorkEffController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventWorkEffUpdated.class,
				event -> sendCommunicationEventWorkEffChangedMessage(((CommunicationEventWorkEffUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventWorkEff from the database
	 * 
	 * @param communicationEventWorkEffId:
	 *            the id of the CommunicationEventWorkEff thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventWorkEffById(@RequestParam(value = "communicationEventWorkEffId") String communicationEventWorkEffId) {

		DeleteCommunicationEventWorkEff com = new DeleteCommunicationEventWorkEff(communicationEventWorkEffId);

		int usedTicketId;

		synchronized (CommunicationEventWorkEffController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventWorkEffDeleted.class,
				event -> sendCommunicationEventWorkEffChangedMessage(((CommunicationEventWorkEffDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventWorkEffChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventWorkEff/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddPartyContent;
import com.skytala.eCommerce.command.DeletePartyContent;
import com.skytala.eCommerce.command.UpdatePartyContent;
import com.skytala.eCommerce.entity.PartyContent;
import com.skytala.eCommerce.entity.PartyContentMapper;
import com.skytala.eCommerce.event.PartyContentAdded;
import com.skytala.eCommerce.event.PartyContentDeleted;
import com.skytala.eCommerce.event.PartyContentFound;
import com.skytala.eCommerce.event.PartyContentUpdated;
import com.skytala.eCommerce.query.FindPartyContentsBy;

@RestController
@RequestMapping("/api/partyContent")
public class PartyContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyContent
	 * @return a List with the PartyContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyContent> findPartyContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyContentsBy query = new FindPartyContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentFound.class,
				event -> sendPartyContentsFoundMessage(((PartyContentFound) event).getPartyContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyContentsFoundMessage(List<PartyContent> partyContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyContents);
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
	public boolean createPartyContent(HttpServletRequest request) {

		PartyContent partyContentToBeAdded = new PartyContent();
		try {
			partyContentToBeAdded = PartyContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyContent(partyContentToBeAdded);

	}

	/**
	 * creates a new PartyContent entry in the ofbiz database
	 * 
	 * @param partyContentToBeAdded
	 *            the PartyContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyContent(PartyContent partyContentToBeAdded) {

		AddPartyContent com = new AddPartyContent(partyContentToBeAdded);
		int usedTicketId;

		synchronized (PartyContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentAdded.class,
				event -> sendPartyContentChangedMessage(((PartyContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyContent(HttpServletRequest request) {

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

		PartyContent partyContentToBeUpdated = new PartyContent();

		try {
			partyContentToBeUpdated = PartyContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyContent(partyContentToBeUpdated);

	}

	/**
	 * Updates the PartyContent with the specific Id
	 * 
	 * @param partyContentToBeUpdated the PartyContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyContent(PartyContent partyContentToBeUpdated) {

		UpdatePartyContent com = new UpdatePartyContent(partyContentToBeUpdated);

		int usedTicketId;

		synchronized (PartyContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentUpdated.class,
				event -> sendPartyContentChangedMessage(((PartyContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyContent from the database
	 * 
	 * @param partyContentId:
	 *            the id of the PartyContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyContentById(@RequestParam(value = "partyContentId") String partyContentId) {

		DeletePartyContent com = new DeletePartyContent(partyContentId);

		int usedTicketId;

		synchronized (PartyContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentDeleted.class,
				event -> sendPartyContentChangedMessage(((PartyContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyContent/\" plus one of the following: "
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

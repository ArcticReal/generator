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
import com.skytala.eCommerce.command.AddPartyTypeAttr;
import com.skytala.eCommerce.command.DeletePartyTypeAttr;
import com.skytala.eCommerce.command.UpdatePartyTypeAttr;
import com.skytala.eCommerce.entity.PartyTypeAttr;
import com.skytala.eCommerce.entity.PartyTypeAttrMapper;
import com.skytala.eCommerce.event.PartyTypeAttrAdded;
import com.skytala.eCommerce.event.PartyTypeAttrDeleted;
import com.skytala.eCommerce.event.PartyTypeAttrFound;
import com.skytala.eCommerce.event.PartyTypeAttrUpdated;
import com.skytala.eCommerce.query.FindPartyTypeAttrsBy;

@RestController
@RequestMapping("/api/partyTypeAttr")
public class PartyTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyTypeAttr
	 * @return a List with the PartyTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyTypeAttr> findPartyTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyTypeAttrsBy query = new FindPartyTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTypeAttrFound.class,
				event -> sendPartyTypeAttrsFoundMessage(((PartyTypeAttrFound) event).getPartyTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyTypeAttrsFoundMessage(List<PartyTypeAttr> partyTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyTypeAttrs);
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
	public boolean createPartyTypeAttr(HttpServletRequest request) {

		PartyTypeAttr partyTypeAttrToBeAdded = new PartyTypeAttr();
		try {
			partyTypeAttrToBeAdded = PartyTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyTypeAttr(partyTypeAttrToBeAdded);

	}

	/**
	 * creates a new PartyTypeAttr entry in the ofbiz database
	 * 
	 * @param partyTypeAttrToBeAdded
	 *            the PartyTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyTypeAttr(PartyTypeAttr partyTypeAttrToBeAdded) {

		AddPartyTypeAttr com = new AddPartyTypeAttr(partyTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (PartyTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTypeAttrAdded.class,
				event -> sendPartyTypeAttrChangedMessage(((PartyTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyTypeAttr(HttpServletRequest request) {

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

		PartyTypeAttr partyTypeAttrToBeUpdated = new PartyTypeAttr();

		try {
			partyTypeAttrToBeUpdated = PartyTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyTypeAttr(partyTypeAttrToBeUpdated);

	}

	/**
	 * Updates the PartyTypeAttr with the specific Id
	 * 
	 * @param partyTypeAttrToBeUpdated the PartyTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyTypeAttr(PartyTypeAttr partyTypeAttrToBeUpdated) {

		UpdatePartyTypeAttr com = new UpdatePartyTypeAttr(partyTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (PartyTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTypeAttrUpdated.class,
				event -> sendPartyTypeAttrChangedMessage(((PartyTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyTypeAttr from the database
	 * 
	 * @param partyTypeAttrId:
	 *            the id of the PartyTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyTypeAttrById(@RequestParam(value = "partyTypeAttrId") String partyTypeAttrId) {

		DeletePartyTypeAttr com = new DeletePartyTypeAttr(partyTypeAttrId);

		int usedTicketId;

		synchronized (PartyTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTypeAttrDeleted.class,
				event -> sendPartyTypeAttrChangedMessage(((PartyTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyTypeAttr/\" plus one of the following: "
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

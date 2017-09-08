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
import com.skytala.eCommerce.command.AddPartyResume;
import com.skytala.eCommerce.command.DeletePartyResume;
import com.skytala.eCommerce.command.UpdatePartyResume;
import com.skytala.eCommerce.entity.PartyResume;
import com.skytala.eCommerce.entity.PartyResumeMapper;
import com.skytala.eCommerce.event.PartyResumeAdded;
import com.skytala.eCommerce.event.PartyResumeDeleted;
import com.skytala.eCommerce.event.PartyResumeFound;
import com.skytala.eCommerce.event.PartyResumeUpdated;
import com.skytala.eCommerce.query.FindPartyResumesBy;

@RestController
@RequestMapping("/api/partyResume")
public class PartyResumeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyResume>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyResumeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyResume
	 * @return a List with the PartyResumes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyResume> findPartyResumesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyResumesBy query = new FindPartyResumesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyResumeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyResumeFound.class,
				event -> sendPartyResumesFoundMessage(((PartyResumeFound) event).getPartyResumes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyResumesFoundMessage(List<PartyResume> partyResumes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyResumes);
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
	public boolean createPartyResume(HttpServletRequest request) {

		PartyResume partyResumeToBeAdded = new PartyResume();
		try {
			partyResumeToBeAdded = PartyResumeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyResume(partyResumeToBeAdded);

	}

	/**
	 * creates a new PartyResume entry in the ofbiz database
	 * 
	 * @param partyResumeToBeAdded
	 *            the PartyResume thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyResume(PartyResume partyResumeToBeAdded) {

		AddPartyResume com = new AddPartyResume(partyResumeToBeAdded);
		int usedTicketId;

		synchronized (PartyResumeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyResumeAdded.class,
				event -> sendPartyResumeChangedMessage(((PartyResumeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyResume(HttpServletRequest request) {

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

		PartyResume partyResumeToBeUpdated = new PartyResume();

		try {
			partyResumeToBeUpdated = PartyResumeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyResume(partyResumeToBeUpdated);

	}

	/**
	 * Updates the PartyResume with the specific Id
	 * 
	 * @param partyResumeToBeUpdated the PartyResume thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyResume(PartyResume partyResumeToBeUpdated) {

		UpdatePartyResume com = new UpdatePartyResume(partyResumeToBeUpdated);

		int usedTicketId;

		synchronized (PartyResumeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyResumeUpdated.class,
				event -> sendPartyResumeChangedMessage(((PartyResumeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyResume from the database
	 * 
	 * @param partyResumeId:
	 *            the id of the PartyResume thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyResumeById(@RequestParam(value = "partyResumeId") String partyResumeId) {

		DeletePartyResume com = new DeletePartyResume(partyResumeId);

		int usedTicketId;

		synchronized (PartyResumeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyResumeDeleted.class,
				event -> sendPartyResumeChangedMessage(((PartyResumeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyResumeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyResume/\" plus one of the following: "
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

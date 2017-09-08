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
import com.skytala.eCommerce.command.AddPartySkill;
import com.skytala.eCommerce.command.DeletePartySkill;
import com.skytala.eCommerce.command.UpdatePartySkill;
import com.skytala.eCommerce.entity.PartySkill;
import com.skytala.eCommerce.entity.PartySkillMapper;
import com.skytala.eCommerce.event.PartySkillAdded;
import com.skytala.eCommerce.event.PartySkillDeleted;
import com.skytala.eCommerce.event.PartySkillFound;
import com.skytala.eCommerce.event.PartySkillUpdated;
import com.skytala.eCommerce.query.FindPartySkillsBy;

@RestController
@RequestMapping("/api/partySkill")
public class PartySkillController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartySkill>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartySkillController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartySkill
	 * @return a List with the PartySkills
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartySkill> findPartySkillsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartySkillsBy query = new FindPartySkillsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartySkillController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartySkillFound.class,
				event -> sendPartySkillsFoundMessage(((PartySkillFound) event).getPartySkills(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartySkillsFoundMessage(List<PartySkill> partySkills, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partySkills);
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
	public boolean createPartySkill(HttpServletRequest request) {

		PartySkill partySkillToBeAdded = new PartySkill();
		try {
			partySkillToBeAdded = PartySkillMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartySkill(partySkillToBeAdded);

	}

	/**
	 * creates a new PartySkill entry in the ofbiz database
	 * 
	 * @param partySkillToBeAdded
	 *            the PartySkill thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartySkill(PartySkill partySkillToBeAdded) {

		AddPartySkill com = new AddPartySkill(partySkillToBeAdded);
		int usedTicketId;

		synchronized (PartySkillController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartySkillAdded.class,
				event -> sendPartySkillChangedMessage(((PartySkillAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartySkill(HttpServletRequest request) {

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

		PartySkill partySkillToBeUpdated = new PartySkill();

		try {
			partySkillToBeUpdated = PartySkillMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartySkill(partySkillToBeUpdated);

	}

	/**
	 * Updates the PartySkill with the specific Id
	 * 
	 * @param partySkillToBeUpdated the PartySkill thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartySkill(PartySkill partySkillToBeUpdated) {

		UpdatePartySkill com = new UpdatePartySkill(partySkillToBeUpdated);

		int usedTicketId;

		synchronized (PartySkillController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartySkillUpdated.class,
				event -> sendPartySkillChangedMessage(((PartySkillUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartySkill from the database
	 * 
	 * @param partySkillId:
	 *            the id of the PartySkill thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartySkillById(@RequestParam(value = "partySkillId") String partySkillId) {

		DeletePartySkill com = new DeletePartySkill(partySkillId);

		int usedTicketId;

		synchronized (PartySkillController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartySkillDeleted.class,
				event -> sendPartySkillChangedMessage(((PartySkillDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartySkillChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partySkill/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddCharacterSet;
import com.skytala.eCommerce.command.DeleteCharacterSet;
import com.skytala.eCommerce.command.UpdateCharacterSet;
import com.skytala.eCommerce.entity.CharacterSet;
import com.skytala.eCommerce.entity.CharacterSetMapper;
import com.skytala.eCommerce.event.CharacterSetAdded;
import com.skytala.eCommerce.event.CharacterSetDeleted;
import com.skytala.eCommerce.event.CharacterSetFound;
import com.skytala.eCommerce.event.CharacterSetUpdated;
import com.skytala.eCommerce.query.FindCharacterSetsBy;

@RestController
@RequestMapping("/api/characterSet")
public class CharacterSetController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CharacterSet>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CharacterSetController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CharacterSet
	 * @return a List with the CharacterSets
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CharacterSet> findCharacterSetsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCharacterSetsBy query = new FindCharacterSetsBy(allRequestParams);

		int usedTicketId;

		synchronized (CharacterSetController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CharacterSetFound.class,
				event -> sendCharacterSetsFoundMessage(((CharacterSetFound) event).getCharacterSets(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCharacterSetsFoundMessage(List<CharacterSet> characterSets, int usedTicketId) {
		queryReturnVal.put(usedTicketId, characterSets);
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
	public boolean createCharacterSet(HttpServletRequest request) {

		CharacterSet characterSetToBeAdded = new CharacterSet();
		try {
			characterSetToBeAdded = CharacterSetMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCharacterSet(characterSetToBeAdded);

	}

	/**
	 * creates a new CharacterSet entry in the ofbiz database
	 * 
	 * @param characterSetToBeAdded
	 *            the CharacterSet thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCharacterSet(CharacterSet characterSetToBeAdded) {

		AddCharacterSet com = new AddCharacterSet(characterSetToBeAdded);
		int usedTicketId;

		synchronized (CharacterSetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CharacterSetAdded.class,
				event -> sendCharacterSetChangedMessage(((CharacterSetAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCharacterSet(HttpServletRequest request) {

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

		CharacterSet characterSetToBeUpdated = new CharacterSet();

		try {
			characterSetToBeUpdated = CharacterSetMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCharacterSet(characterSetToBeUpdated);

	}

	/**
	 * Updates the CharacterSet with the specific Id
	 * 
	 * @param characterSetToBeUpdated the CharacterSet thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCharacterSet(CharacterSet characterSetToBeUpdated) {

		UpdateCharacterSet com = new UpdateCharacterSet(characterSetToBeUpdated);

		int usedTicketId;

		synchronized (CharacterSetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CharacterSetUpdated.class,
				event -> sendCharacterSetChangedMessage(((CharacterSetUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CharacterSet from the database
	 * 
	 * @param characterSetId:
	 *            the id of the CharacterSet thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecharacterSetById(@RequestParam(value = "characterSetId") String characterSetId) {

		DeleteCharacterSet com = new DeleteCharacterSet(characterSetId);

		int usedTicketId;

		synchronized (CharacterSetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CharacterSetDeleted.class,
				event -> sendCharacterSetChangedMessage(((CharacterSetDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCharacterSetChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/characterSet/\" plus one of the following: "
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

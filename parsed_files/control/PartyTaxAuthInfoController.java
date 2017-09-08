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
import com.skytala.eCommerce.command.AddPartyTaxAuthInfo;
import com.skytala.eCommerce.command.DeletePartyTaxAuthInfo;
import com.skytala.eCommerce.command.UpdatePartyTaxAuthInfo;
import com.skytala.eCommerce.entity.PartyTaxAuthInfo;
import com.skytala.eCommerce.entity.PartyTaxAuthInfoMapper;
import com.skytala.eCommerce.event.PartyTaxAuthInfoAdded;
import com.skytala.eCommerce.event.PartyTaxAuthInfoDeleted;
import com.skytala.eCommerce.event.PartyTaxAuthInfoFound;
import com.skytala.eCommerce.event.PartyTaxAuthInfoUpdated;
import com.skytala.eCommerce.query.FindPartyTaxAuthInfosBy;

@RestController
@RequestMapping("/api/partyTaxAuthInfo")
public class PartyTaxAuthInfoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyTaxAuthInfo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyTaxAuthInfoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyTaxAuthInfo
	 * @return a List with the PartyTaxAuthInfos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyTaxAuthInfo> findPartyTaxAuthInfosBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyTaxAuthInfosBy query = new FindPartyTaxAuthInfosBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyTaxAuthInfoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTaxAuthInfoFound.class,
				event -> sendPartyTaxAuthInfosFoundMessage(((PartyTaxAuthInfoFound) event).getPartyTaxAuthInfos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyTaxAuthInfosFoundMessage(List<PartyTaxAuthInfo> partyTaxAuthInfos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyTaxAuthInfos);
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
	public boolean createPartyTaxAuthInfo(HttpServletRequest request) {

		PartyTaxAuthInfo partyTaxAuthInfoToBeAdded = new PartyTaxAuthInfo();
		try {
			partyTaxAuthInfoToBeAdded = PartyTaxAuthInfoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyTaxAuthInfo(partyTaxAuthInfoToBeAdded);

	}

	/**
	 * creates a new PartyTaxAuthInfo entry in the ofbiz database
	 * 
	 * @param partyTaxAuthInfoToBeAdded
	 *            the PartyTaxAuthInfo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyTaxAuthInfo(PartyTaxAuthInfo partyTaxAuthInfoToBeAdded) {

		AddPartyTaxAuthInfo com = new AddPartyTaxAuthInfo(partyTaxAuthInfoToBeAdded);
		int usedTicketId;

		synchronized (PartyTaxAuthInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTaxAuthInfoAdded.class,
				event -> sendPartyTaxAuthInfoChangedMessage(((PartyTaxAuthInfoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyTaxAuthInfo(HttpServletRequest request) {

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

		PartyTaxAuthInfo partyTaxAuthInfoToBeUpdated = new PartyTaxAuthInfo();

		try {
			partyTaxAuthInfoToBeUpdated = PartyTaxAuthInfoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyTaxAuthInfo(partyTaxAuthInfoToBeUpdated);

	}

	/**
	 * Updates the PartyTaxAuthInfo with the specific Id
	 * 
	 * @param partyTaxAuthInfoToBeUpdated the PartyTaxAuthInfo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyTaxAuthInfo(PartyTaxAuthInfo partyTaxAuthInfoToBeUpdated) {

		UpdatePartyTaxAuthInfo com = new UpdatePartyTaxAuthInfo(partyTaxAuthInfoToBeUpdated);

		int usedTicketId;

		synchronized (PartyTaxAuthInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTaxAuthInfoUpdated.class,
				event -> sendPartyTaxAuthInfoChangedMessage(((PartyTaxAuthInfoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyTaxAuthInfo from the database
	 * 
	 * @param partyTaxAuthInfoId:
	 *            the id of the PartyTaxAuthInfo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyTaxAuthInfoById(@RequestParam(value = "partyTaxAuthInfoId") String partyTaxAuthInfoId) {

		DeletePartyTaxAuthInfo com = new DeletePartyTaxAuthInfo(partyTaxAuthInfoId);

		int usedTicketId;

		synchronized (PartyTaxAuthInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyTaxAuthInfoDeleted.class,
				event -> sendPartyTaxAuthInfoChangedMessage(((PartyTaxAuthInfoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyTaxAuthInfoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyTaxAuthInfo/\" plus one of the following: "
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

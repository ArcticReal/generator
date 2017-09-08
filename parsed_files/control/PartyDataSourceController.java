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
import com.skytala.eCommerce.command.AddPartyDataSource;
import com.skytala.eCommerce.command.DeletePartyDataSource;
import com.skytala.eCommerce.command.UpdatePartyDataSource;
import com.skytala.eCommerce.entity.PartyDataSource;
import com.skytala.eCommerce.entity.PartyDataSourceMapper;
import com.skytala.eCommerce.event.PartyDataSourceAdded;
import com.skytala.eCommerce.event.PartyDataSourceDeleted;
import com.skytala.eCommerce.event.PartyDataSourceFound;
import com.skytala.eCommerce.event.PartyDataSourceUpdated;
import com.skytala.eCommerce.query.FindPartyDataSourcesBy;

@RestController
@RequestMapping("/api/partyDataSource")
public class PartyDataSourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyDataSource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyDataSourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyDataSource
	 * @return a List with the PartyDataSources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyDataSource> findPartyDataSourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyDataSourcesBy query = new FindPartyDataSourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyDataSourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyDataSourceFound.class,
				event -> sendPartyDataSourcesFoundMessage(((PartyDataSourceFound) event).getPartyDataSources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyDataSourcesFoundMessage(List<PartyDataSource> partyDataSources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyDataSources);
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
	public boolean createPartyDataSource(HttpServletRequest request) {

		PartyDataSource partyDataSourceToBeAdded = new PartyDataSource();
		try {
			partyDataSourceToBeAdded = PartyDataSourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyDataSource(partyDataSourceToBeAdded);

	}

	/**
	 * creates a new PartyDataSource entry in the ofbiz database
	 * 
	 * @param partyDataSourceToBeAdded
	 *            the PartyDataSource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyDataSource(PartyDataSource partyDataSourceToBeAdded) {

		AddPartyDataSource com = new AddPartyDataSource(partyDataSourceToBeAdded);
		int usedTicketId;

		synchronized (PartyDataSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyDataSourceAdded.class,
				event -> sendPartyDataSourceChangedMessage(((PartyDataSourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyDataSource(HttpServletRequest request) {

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

		PartyDataSource partyDataSourceToBeUpdated = new PartyDataSource();

		try {
			partyDataSourceToBeUpdated = PartyDataSourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyDataSource(partyDataSourceToBeUpdated);

	}

	/**
	 * Updates the PartyDataSource with the specific Id
	 * 
	 * @param partyDataSourceToBeUpdated the PartyDataSource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyDataSource(PartyDataSource partyDataSourceToBeUpdated) {

		UpdatePartyDataSource com = new UpdatePartyDataSource(partyDataSourceToBeUpdated);

		int usedTicketId;

		synchronized (PartyDataSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyDataSourceUpdated.class,
				event -> sendPartyDataSourceChangedMessage(((PartyDataSourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyDataSource from the database
	 * 
	 * @param partyDataSourceId:
	 *            the id of the PartyDataSource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyDataSourceById(@RequestParam(value = "partyDataSourceId") String partyDataSourceId) {

		DeletePartyDataSource com = new DeletePartyDataSource(partyDataSourceId);

		int usedTicketId;

		synchronized (PartyDataSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyDataSourceDeleted.class,
				event -> sendPartyDataSourceChangedMessage(((PartyDataSourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyDataSourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyDataSource/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddMarketingCampaignNote;
import com.skytala.eCommerce.command.DeleteMarketingCampaignNote;
import com.skytala.eCommerce.command.UpdateMarketingCampaignNote;
import com.skytala.eCommerce.entity.MarketingCampaignNote;
import com.skytala.eCommerce.entity.MarketingCampaignNoteMapper;
import com.skytala.eCommerce.event.MarketingCampaignNoteAdded;
import com.skytala.eCommerce.event.MarketingCampaignNoteDeleted;
import com.skytala.eCommerce.event.MarketingCampaignNoteFound;
import com.skytala.eCommerce.event.MarketingCampaignNoteUpdated;
import com.skytala.eCommerce.query.FindMarketingCampaignNotesBy;

@RestController
@RequestMapping("/api/marketingCampaignNote")
public class MarketingCampaignNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MarketingCampaignNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MarketingCampaignNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MarketingCampaignNote
	 * @return a List with the MarketingCampaignNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MarketingCampaignNote> findMarketingCampaignNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMarketingCampaignNotesBy query = new FindMarketingCampaignNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (MarketingCampaignNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignNoteFound.class,
				event -> sendMarketingCampaignNotesFoundMessage(((MarketingCampaignNoteFound) event).getMarketingCampaignNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMarketingCampaignNotesFoundMessage(List<MarketingCampaignNote> marketingCampaignNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, marketingCampaignNotes);
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
	public boolean createMarketingCampaignNote(HttpServletRequest request) {

		MarketingCampaignNote marketingCampaignNoteToBeAdded = new MarketingCampaignNote();
		try {
			marketingCampaignNoteToBeAdded = MarketingCampaignNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMarketingCampaignNote(marketingCampaignNoteToBeAdded);

	}

	/**
	 * creates a new MarketingCampaignNote entry in the ofbiz database
	 * 
	 * @param marketingCampaignNoteToBeAdded
	 *            the MarketingCampaignNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMarketingCampaignNote(MarketingCampaignNote marketingCampaignNoteToBeAdded) {

		AddMarketingCampaignNote com = new AddMarketingCampaignNote(marketingCampaignNoteToBeAdded);
		int usedTicketId;

		synchronized (MarketingCampaignNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignNoteAdded.class,
				event -> sendMarketingCampaignNoteChangedMessage(((MarketingCampaignNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMarketingCampaignNote(HttpServletRequest request) {

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

		MarketingCampaignNote marketingCampaignNoteToBeUpdated = new MarketingCampaignNote();

		try {
			marketingCampaignNoteToBeUpdated = MarketingCampaignNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMarketingCampaignNote(marketingCampaignNoteToBeUpdated);

	}

	/**
	 * Updates the MarketingCampaignNote with the specific Id
	 * 
	 * @param marketingCampaignNoteToBeUpdated the MarketingCampaignNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMarketingCampaignNote(MarketingCampaignNote marketingCampaignNoteToBeUpdated) {

		UpdateMarketingCampaignNote com = new UpdateMarketingCampaignNote(marketingCampaignNoteToBeUpdated);

		int usedTicketId;

		synchronized (MarketingCampaignNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignNoteUpdated.class,
				event -> sendMarketingCampaignNoteChangedMessage(((MarketingCampaignNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MarketingCampaignNote from the database
	 * 
	 * @param marketingCampaignNoteId:
	 *            the id of the MarketingCampaignNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemarketingCampaignNoteById(@RequestParam(value = "marketingCampaignNoteId") String marketingCampaignNoteId) {

		DeleteMarketingCampaignNote com = new DeleteMarketingCampaignNote(marketingCampaignNoteId);

		int usedTicketId;

		synchronized (MarketingCampaignNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignNoteDeleted.class,
				event -> sendMarketingCampaignNoteChangedMessage(((MarketingCampaignNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMarketingCampaignNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/marketingCampaignNote/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddSalesForecastHistory;
import com.skytala.eCommerce.command.DeleteSalesForecastHistory;
import com.skytala.eCommerce.command.UpdateSalesForecastHistory;
import com.skytala.eCommerce.entity.SalesForecastHistory;
import com.skytala.eCommerce.entity.SalesForecastHistoryMapper;
import com.skytala.eCommerce.event.SalesForecastHistoryAdded;
import com.skytala.eCommerce.event.SalesForecastHistoryDeleted;
import com.skytala.eCommerce.event.SalesForecastHistoryFound;
import com.skytala.eCommerce.event.SalesForecastHistoryUpdated;
import com.skytala.eCommerce.query.FindSalesForecastHistorysBy;

@RestController
@RequestMapping("/api/salesForecastHistory")
public class SalesForecastHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesForecastHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesForecastHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesForecastHistory
	 * @return a List with the SalesForecastHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesForecastHistory> findSalesForecastHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesForecastHistorysBy query = new FindSalesForecastHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesForecastHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastHistoryFound.class,
				event -> sendSalesForecastHistorysFoundMessage(((SalesForecastHistoryFound) event).getSalesForecastHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesForecastHistorysFoundMessage(List<SalesForecastHistory> salesForecastHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesForecastHistorys);
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
	public boolean createSalesForecastHistory(HttpServletRequest request) {

		SalesForecastHistory salesForecastHistoryToBeAdded = new SalesForecastHistory();
		try {
			salesForecastHistoryToBeAdded = SalesForecastHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesForecastHistory(salesForecastHistoryToBeAdded);

	}

	/**
	 * creates a new SalesForecastHistory entry in the ofbiz database
	 * 
	 * @param salesForecastHistoryToBeAdded
	 *            the SalesForecastHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesForecastHistory(SalesForecastHistory salesForecastHistoryToBeAdded) {

		AddSalesForecastHistory com = new AddSalesForecastHistory(salesForecastHistoryToBeAdded);
		int usedTicketId;

		synchronized (SalesForecastHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastHistoryAdded.class,
				event -> sendSalesForecastHistoryChangedMessage(((SalesForecastHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesForecastHistory(HttpServletRequest request) {

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

		SalesForecastHistory salesForecastHistoryToBeUpdated = new SalesForecastHistory();

		try {
			salesForecastHistoryToBeUpdated = SalesForecastHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesForecastHistory(salesForecastHistoryToBeUpdated);

	}

	/**
	 * Updates the SalesForecastHistory with the specific Id
	 * 
	 * @param salesForecastHistoryToBeUpdated the SalesForecastHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesForecastHistory(SalesForecastHistory salesForecastHistoryToBeUpdated) {

		UpdateSalesForecastHistory com = new UpdateSalesForecastHistory(salesForecastHistoryToBeUpdated);

		int usedTicketId;

		synchronized (SalesForecastHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastHistoryUpdated.class,
				event -> sendSalesForecastHistoryChangedMessage(((SalesForecastHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesForecastHistory from the database
	 * 
	 * @param salesForecastHistoryId:
	 *            the id of the SalesForecastHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesForecastHistoryById(@RequestParam(value = "salesForecastHistoryId") String salesForecastHistoryId) {

		DeleteSalesForecastHistory com = new DeleteSalesForecastHistory(salesForecastHistoryId);

		int usedTicketId;

		synchronized (SalesForecastHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastHistoryDeleted.class,
				event -> sendSalesForecastHistoryChangedMessage(((SalesForecastHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesForecastHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesForecastHistory/\" plus one of the following: "
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

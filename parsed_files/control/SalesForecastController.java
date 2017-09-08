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
import com.skytala.eCommerce.command.AddSalesForecast;
import com.skytala.eCommerce.command.DeleteSalesForecast;
import com.skytala.eCommerce.command.UpdateSalesForecast;
import com.skytala.eCommerce.entity.SalesForecast;
import com.skytala.eCommerce.entity.SalesForecastMapper;
import com.skytala.eCommerce.event.SalesForecastAdded;
import com.skytala.eCommerce.event.SalesForecastDeleted;
import com.skytala.eCommerce.event.SalesForecastFound;
import com.skytala.eCommerce.event.SalesForecastUpdated;
import com.skytala.eCommerce.query.FindSalesForecastsBy;

@RestController
@RequestMapping("/api/salesForecast")
public class SalesForecastController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesForecast>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesForecastController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesForecast
	 * @return a List with the SalesForecasts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesForecast> findSalesForecastsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesForecastsBy query = new FindSalesForecastsBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesForecastController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastFound.class,
				event -> sendSalesForecastsFoundMessage(((SalesForecastFound) event).getSalesForecasts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesForecastsFoundMessage(List<SalesForecast> salesForecasts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesForecasts);
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
	public boolean createSalesForecast(HttpServletRequest request) {

		SalesForecast salesForecastToBeAdded = new SalesForecast();
		try {
			salesForecastToBeAdded = SalesForecastMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesForecast(salesForecastToBeAdded);

	}

	/**
	 * creates a new SalesForecast entry in the ofbiz database
	 * 
	 * @param salesForecastToBeAdded
	 *            the SalesForecast thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesForecast(SalesForecast salesForecastToBeAdded) {

		AddSalesForecast com = new AddSalesForecast(salesForecastToBeAdded);
		int usedTicketId;

		synchronized (SalesForecastController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastAdded.class,
				event -> sendSalesForecastChangedMessage(((SalesForecastAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesForecast(HttpServletRequest request) {

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

		SalesForecast salesForecastToBeUpdated = new SalesForecast();

		try {
			salesForecastToBeUpdated = SalesForecastMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesForecast(salesForecastToBeUpdated);

	}

	/**
	 * Updates the SalesForecast with the specific Id
	 * 
	 * @param salesForecastToBeUpdated the SalesForecast thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesForecast(SalesForecast salesForecastToBeUpdated) {

		UpdateSalesForecast com = new UpdateSalesForecast(salesForecastToBeUpdated);

		int usedTicketId;

		synchronized (SalesForecastController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastUpdated.class,
				event -> sendSalesForecastChangedMessage(((SalesForecastUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesForecast from the database
	 * 
	 * @param salesForecastId:
	 *            the id of the SalesForecast thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesForecastById(@RequestParam(value = "salesForecastId") String salesForecastId) {

		DeleteSalesForecast com = new DeleteSalesForecast(salesForecastId);

		int usedTicketId;

		synchronized (SalesForecastController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastDeleted.class,
				event -> sendSalesForecastChangedMessage(((SalesForecastDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesForecastChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesForecast/\" plus one of the following: "
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

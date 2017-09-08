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
import com.skytala.eCommerce.command.AddSalesForecastDetail;
import com.skytala.eCommerce.command.DeleteSalesForecastDetail;
import com.skytala.eCommerce.command.UpdateSalesForecastDetail;
import com.skytala.eCommerce.entity.SalesForecastDetail;
import com.skytala.eCommerce.entity.SalesForecastDetailMapper;
import com.skytala.eCommerce.event.SalesForecastDetailAdded;
import com.skytala.eCommerce.event.SalesForecastDetailDeleted;
import com.skytala.eCommerce.event.SalesForecastDetailFound;
import com.skytala.eCommerce.event.SalesForecastDetailUpdated;
import com.skytala.eCommerce.query.FindSalesForecastDetailsBy;

@RestController
@RequestMapping("/api/salesForecastDetail")
public class SalesForecastDetailController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesForecastDetail>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesForecastDetailController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesForecastDetail
	 * @return a List with the SalesForecastDetails
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesForecastDetail> findSalesForecastDetailsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesForecastDetailsBy query = new FindSalesForecastDetailsBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesForecastDetailController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastDetailFound.class,
				event -> sendSalesForecastDetailsFoundMessage(((SalesForecastDetailFound) event).getSalesForecastDetails(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesForecastDetailsFoundMessage(List<SalesForecastDetail> salesForecastDetails, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesForecastDetails);
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
	public boolean createSalesForecastDetail(HttpServletRequest request) {

		SalesForecastDetail salesForecastDetailToBeAdded = new SalesForecastDetail();
		try {
			salesForecastDetailToBeAdded = SalesForecastDetailMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesForecastDetail(salesForecastDetailToBeAdded);

	}

	/**
	 * creates a new SalesForecastDetail entry in the ofbiz database
	 * 
	 * @param salesForecastDetailToBeAdded
	 *            the SalesForecastDetail thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesForecastDetail(SalesForecastDetail salesForecastDetailToBeAdded) {

		AddSalesForecastDetail com = new AddSalesForecastDetail(salesForecastDetailToBeAdded);
		int usedTicketId;

		synchronized (SalesForecastDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastDetailAdded.class,
				event -> sendSalesForecastDetailChangedMessage(((SalesForecastDetailAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesForecastDetail(HttpServletRequest request) {

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

		SalesForecastDetail salesForecastDetailToBeUpdated = new SalesForecastDetail();

		try {
			salesForecastDetailToBeUpdated = SalesForecastDetailMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesForecastDetail(salesForecastDetailToBeUpdated);

	}

	/**
	 * Updates the SalesForecastDetail with the specific Id
	 * 
	 * @param salesForecastDetailToBeUpdated the SalesForecastDetail thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesForecastDetail(SalesForecastDetail salesForecastDetailToBeUpdated) {

		UpdateSalesForecastDetail com = new UpdateSalesForecastDetail(salesForecastDetailToBeUpdated);

		int usedTicketId;

		synchronized (SalesForecastDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastDetailUpdated.class,
				event -> sendSalesForecastDetailChangedMessage(((SalesForecastDetailUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesForecastDetail from the database
	 * 
	 * @param salesForecastDetailId:
	 *            the id of the SalesForecastDetail thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesForecastDetailById(@RequestParam(value = "salesForecastDetailId") String salesForecastDetailId) {

		DeleteSalesForecastDetail com = new DeleteSalesForecastDetail(salesForecastDetailId);

		int usedTicketId;

		synchronized (SalesForecastDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesForecastDetailDeleted.class,
				event -> sendSalesForecastDetailChangedMessage(((SalesForecastDetailDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesForecastDetailChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesForecastDetail/\" plus one of the following: "
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

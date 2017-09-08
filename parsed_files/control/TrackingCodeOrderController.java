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
import com.skytala.eCommerce.command.AddTrackingCodeOrder;
import com.skytala.eCommerce.command.DeleteTrackingCodeOrder;
import com.skytala.eCommerce.command.UpdateTrackingCodeOrder;
import com.skytala.eCommerce.entity.TrackingCodeOrder;
import com.skytala.eCommerce.entity.TrackingCodeOrderMapper;
import com.skytala.eCommerce.event.TrackingCodeOrderAdded;
import com.skytala.eCommerce.event.TrackingCodeOrderDeleted;
import com.skytala.eCommerce.event.TrackingCodeOrderFound;
import com.skytala.eCommerce.event.TrackingCodeOrderUpdated;
import com.skytala.eCommerce.query.FindTrackingCodeOrdersBy;

@RestController
@RequestMapping("/api/trackingCodeOrder")
public class TrackingCodeOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrackingCodeOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrackingCodeOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrackingCodeOrder
	 * @return a List with the TrackingCodeOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrackingCodeOrder> findTrackingCodeOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrackingCodeOrdersBy query = new FindTrackingCodeOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (TrackingCodeOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderFound.class,
				event -> sendTrackingCodeOrdersFoundMessage(((TrackingCodeOrderFound) event).getTrackingCodeOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrackingCodeOrdersFoundMessage(List<TrackingCodeOrder> trackingCodeOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trackingCodeOrders);
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
	public boolean createTrackingCodeOrder(HttpServletRequest request) {

		TrackingCodeOrder trackingCodeOrderToBeAdded = new TrackingCodeOrder();
		try {
			trackingCodeOrderToBeAdded = TrackingCodeOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrackingCodeOrder(trackingCodeOrderToBeAdded);

	}

	/**
	 * creates a new TrackingCodeOrder entry in the ofbiz database
	 * 
	 * @param trackingCodeOrderToBeAdded
	 *            the TrackingCodeOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrackingCodeOrder(TrackingCodeOrder trackingCodeOrderToBeAdded) {

		AddTrackingCodeOrder com = new AddTrackingCodeOrder(trackingCodeOrderToBeAdded);
		int usedTicketId;

		synchronized (TrackingCodeOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderAdded.class,
				event -> sendTrackingCodeOrderChangedMessage(((TrackingCodeOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrackingCodeOrder(HttpServletRequest request) {

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

		TrackingCodeOrder trackingCodeOrderToBeUpdated = new TrackingCodeOrder();

		try {
			trackingCodeOrderToBeUpdated = TrackingCodeOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrackingCodeOrder(trackingCodeOrderToBeUpdated);

	}

	/**
	 * Updates the TrackingCodeOrder with the specific Id
	 * 
	 * @param trackingCodeOrderToBeUpdated the TrackingCodeOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrackingCodeOrder(TrackingCodeOrder trackingCodeOrderToBeUpdated) {

		UpdateTrackingCodeOrder com = new UpdateTrackingCodeOrder(trackingCodeOrderToBeUpdated);

		int usedTicketId;

		synchronized (TrackingCodeOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderUpdated.class,
				event -> sendTrackingCodeOrderChangedMessage(((TrackingCodeOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrackingCodeOrder from the database
	 * 
	 * @param trackingCodeOrderId:
	 *            the id of the TrackingCodeOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrackingCodeOrderById(@RequestParam(value = "trackingCodeOrderId") String trackingCodeOrderId) {

		DeleteTrackingCodeOrder com = new DeleteTrackingCodeOrder(trackingCodeOrderId);

		int usedTicketId;

		synchronized (TrackingCodeOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderDeleted.class,
				event -> sendTrackingCodeOrderChangedMessage(((TrackingCodeOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrackingCodeOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trackingCodeOrder/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddOrderItemPriceInfo;
import com.skytala.eCommerce.command.DeleteOrderItemPriceInfo;
import com.skytala.eCommerce.command.UpdateOrderItemPriceInfo;
import com.skytala.eCommerce.entity.OrderItemPriceInfo;
import com.skytala.eCommerce.entity.OrderItemPriceInfoMapper;
import com.skytala.eCommerce.event.OrderItemPriceInfoAdded;
import com.skytala.eCommerce.event.OrderItemPriceInfoDeleted;
import com.skytala.eCommerce.event.OrderItemPriceInfoFound;
import com.skytala.eCommerce.event.OrderItemPriceInfoUpdated;
import com.skytala.eCommerce.query.FindOrderItemPriceInfosBy;

@RestController
@RequestMapping("/api/orderItemPriceInfo")
public class OrderItemPriceInfoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemPriceInfo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemPriceInfoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemPriceInfo
	 * @return a List with the OrderItemPriceInfos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemPriceInfo> findOrderItemPriceInfosBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemPriceInfosBy query = new FindOrderItemPriceInfosBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemPriceInfoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemPriceInfoFound.class,
				event -> sendOrderItemPriceInfosFoundMessage(((OrderItemPriceInfoFound) event).getOrderItemPriceInfos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemPriceInfosFoundMessage(List<OrderItemPriceInfo> orderItemPriceInfos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemPriceInfos);
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
	public boolean createOrderItemPriceInfo(HttpServletRequest request) {

		OrderItemPriceInfo orderItemPriceInfoToBeAdded = new OrderItemPriceInfo();
		try {
			orderItemPriceInfoToBeAdded = OrderItemPriceInfoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemPriceInfo(orderItemPriceInfoToBeAdded);

	}

	/**
	 * creates a new OrderItemPriceInfo entry in the ofbiz database
	 * 
	 * @param orderItemPriceInfoToBeAdded
	 *            the OrderItemPriceInfo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemPriceInfo(OrderItemPriceInfo orderItemPriceInfoToBeAdded) {

		AddOrderItemPriceInfo com = new AddOrderItemPriceInfo(orderItemPriceInfoToBeAdded);
		int usedTicketId;

		synchronized (OrderItemPriceInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemPriceInfoAdded.class,
				event -> sendOrderItemPriceInfoChangedMessage(((OrderItemPriceInfoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemPriceInfo(HttpServletRequest request) {

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

		OrderItemPriceInfo orderItemPriceInfoToBeUpdated = new OrderItemPriceInfo();

		try {
			orderItemPriceInfoToBeUpdated = OrderItemPriceInfoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemPriceInfo(orderItemPriceInfoToBeUpdated);

	}

	/**
	 * Updates the OrderItemPriceInfo with the specific Id
	 * 
	 * @param orderItemPriceInfoToBeUpdated the OrderItemPriceInfo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemPriceInfo(OrderItemPriceInfo orderItemPriceInfoToBeUpdated) {

		UpdateOrderItemPriceInfo com = new UpdateOrderItemPriceInfo(orderItemPriceInfoToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemPriceInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemPriceInfoUpdated.class,
				event -> sendOrderItemPriceInfoChangedMessage(((OrderItemPriceInfoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemPriceInfo from the database
	 * 
	 * @param orderItemPriceInfoId:
	 *            the id of the OrderItemPriceInfo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemPriceInfoById(@RequestParam(value = "orderItemPriceInfoId") String orderItemPriceInfoId) {

		DeleteOrderItemPriceInfo com = new DeleteOrderItemPriceInfo(orderItemPriceInfoId);

		int usedTicketId;

		synchronized (OrderItemPriceInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemPriceInfoDeleted.class,
				event -> sendOrderItemPriceInfoChangedMessage(((OrderItemPriceInfoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemPriceInfoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemPriceInfo/\" plus one of the following: "
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

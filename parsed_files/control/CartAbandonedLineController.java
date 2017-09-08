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
import com.skytala.eCommerce.command.AddCartAbandonedLine;
import com.skytala.eCommerce.command.DeleteCartAbandonedLine;
import com.skytala.eCommerce.command.UpdateCartAbandonedLine;
import com.skytala.eCommerce.entity.CartAbandonedLine;
import com.skytala.eCommerce.entity.CartAbandonedLineMapper;
import com.skytala.eCommerce.event.CartAbandonedLineAdded;
import com.skytala.eCommerce.event.CartAbandonedLineDeleted;
import com.skytala.eCommerce.event.CartAbandonedLineFound;
import com.skytala.eCommerce.event.CartAbandonedLineUpdated;
import com.skytala.eCommerce.query.FindCartAbandonedLinesBy;

@RestController
@RequestMapping("/api/cartAbandonedLine")
public class CartAbandonedLineController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CartAbandonedLine>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CartAbandonedLineController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CartAbandonedLine
	 * @return a List with the CartAbandonedLines
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CartAbandonedLine> findCartAbandonedLinesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCartAbandonedLinesBy query = new FindCartAbandonedLinesBy(allRequestParams);

		int usedTicketId;

		synchronized (CartAbandonedLineController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CartAbandonedLineFound.class,
				event -> sendCartAbandonedLinesFoundMessage(((CartAbandonedLineFound) event).getCartAbandonedLines(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCartAbandonedLinesFoundMessage(List<CartAbandonedLine> cartAbandonedLines, int usedTicketId) {
		queryReturnVal.put(usedTicketId, cartAbandonedLines);
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
	public boolean createCartAbandonedLine(HttpServletRequest request) {

		CartAbandonedLine cartAbandonedLineToBeAdded = new CartAbandonedLine();
		try {
			cartAbandonedLineToBeAdded = CartAbandonedLineMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCartAbandonedLine(cartAbandonedLineToBeAdded);

	}

	/**
	 * creates a new CartAbandonedLine entry in the ofbiz database
	 * 
	 * @param cartAbandonedLineToBeAdded
	 *            the CartAbandonedLine thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCartAbandonedLine(CartAbandonedLine cartAbandonedLineToBeAdded) {

		AddCartAbandonedLine com = new AddCartAbandonedLine(cartAbandonedLineToBeAdded);
		int usedTicketId;

		synchronized (CartAbandonedLineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CartAbandonedLineAdded.class,
				event -> sendCartAbandonedLineChangedMessage(((CartAbandonedLineAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCartAbandonedLine(HttpServletRequest request) {

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

		CartAbandonedLine cartAbandonedLineToBeUpdated = new CartAbandonedLine();

		try {
			cartAbandonedLineToBeUpdated = CartAbandonedLineMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCartAbandonedLine(cartAbandonedLineToBeUpdated);

	}

	/**
	 * Updates the CartAbandonedLine with the specific Id
	 * 
	 * @param cartAbandonedLineToBeUpdated the CartAbandonedLine thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCartAbandonedLine(CartAbandonedLine cartAbandonedLineToBeUpdated) {

		UpdateCartAbandonedLine com = new UpdateCartAbandonedLine(cartAbandonedLineToBeUpdated);

		int usedTicketId;

		synchronized (CartAbandonedLineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CartAbandonedLineUpdated.class,
				event -> sendCartAbandonedLineChangedMessage(((CartAbandonedLineUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CartAbandonedLine from the database
	 * 
	 * @param cartAbandonedLineId:
	 *            the id of the CartAbandonedLine thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecartAbandonedLineById(@RequestParam(value = "cartAbandonedLineId") String cartAbandonedLineId) {

		DeleteCartAbandonedLine com = new DeleteCartAbandonedLine(cartAbandonedLineId);

		int usedTicketId;

		synchronized (CartAbandonedLineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CartAbandonedLineDeleted.class,
				event -> sendCartAbandonedLineChangedMessage(((CartAbandonedLineDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCartAbandonedLineChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/cartAbandonedLine/\" plus one of the following: "
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

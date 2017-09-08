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
import com.skytala.eCommerce.command.AddProductPromoCond;
import com.skytala.eCommerce.command.DeleteProductPromoCond;
import com.skytala.eCommerce.command.UpdateProductPromoCond;
import com.skytala.eCommerce.entity.ProductPromoCond;
import com.skytala.eCommerce.entity.ProductPromoCondMapper;
import com.skytala.eCommerce.event.ProductPromoCondAdded;
import com.skytala.eCommerce.event.ProductPromoCondDeleted;
import com.skytala.eCommerce.event.ProductPromoCondFound;
import com.skytala.eCommerce.event.ProductPromoCondUpdated;
import com.skytala.eCommerce.query.FindProductPromoCondsBy;

@RestController
@RequestMapping("/api/productPromoCond")
public class ProductPromoCondController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoCond>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoCondController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoCond
	 * @return a List with the ProductPromoConds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoCond> findProductPromoCondsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoCondsBy query = new FindProductPromoCondsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoCondController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCondFound.class,
				event -> sendProductPromoCondsFoundMessage(((ProductPromoCondFound) event).getProductPromoConds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoCondsFoundMessage(List<ProductPromoCond> productPromoConds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoConds);
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
	public boolean createProductPromoCond(HttpServletRequest request) {

		ProductPromoCond productPromoCondToBeAdded = new ProductPromoCond();
		try {
			productPromoCondToBeAdded = ProductPromoCondMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoCond(productPromoCondToBeAdded);

	}

	/**
	 * creates a new ProductPromoCond entry in the ofbiz database
	 * 
	 * @param productPromoCondToBeAdded
	 *            the ProductPromoCond thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoCond(ProductPromoCond productPromoCondToBeAdded) {

		AddProductPromoCond com = new AddProductPromoCond(productPromoCondToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCondAdded.class,
				event -> sendProductPromoCondChangedMessage(((ProductPromoCondAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoCond(HttpServletRequest request) {

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

		ProductPromoCond productPromoCondToBeUpdated = new ProductPromoCond();

		try {
			productPromoCondToBeUpdated = ProductPromoCondMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoCond(productPromoCondToBeUpdated);

	}

	/**
	 * Updates the ProductPromoCond with the specific Id
	 * 
	 * @param productPromoCondToBeUpdated the ProductPromoCond thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoCond(ProductPromoCond productPromoCondToBeUpdated) {

		UpdateProductPromoCond com = new UpdateProductPromoCond(productPromoCondToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCondUpdated.class,
				event -> sendProductPromoCondChangedMessage(((ProductPromoCondUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoCond from the database
	 * 
	 * @param productPromoCondId:
	 *            the id of the ProductPromoCond thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoCondById(@RequestParam(value = "productPromoCondId") String productPromoCondId) {

		DeleteProductPromoCond com = new DeleteProductPromoCond(productPromoCondId);

		int usedTicketId;

		synchronized (ProductPromoCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCondDeleted.class,
				event -> sendProductPromoCondChangedMessage(((ProductPromoCondDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoCondChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoCond/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddProductConfigOption;
import com.skytala.eCommerce.command.DeleteProductConfigOption;
import com.skytala.eCommerce.command.UpdateProductConfigOption;
import com.skytala.eCommerce.entity.ProductConfigOption;
import com.skytala.eCommerce.entity.ProductConfigOptionMapper;
import com.skytala.eCommerce.event.ProductConfigOptionAdded;
import com.skytala.eCommerce.event.ProductConfigOptionDeleted;
import com.skytala.eCommerce.event.ProductConfigOptionFound;
import com.skytala.eCommerce.event.ProductConfigOptionUpdated;
import com.skytala.eCommerce.query.FindProductConfigOptionsBy;

@RestController
@RequestMapping("/api/productConfigOption")
public class ProductConfigOptionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigOption>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigOptionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigOption
	 * @return a List with the ProductConfigOptions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigOption> findProductConfigOptionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigOptionsBy query = new FindProductConfigOptionsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigOptionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionFound.class,
				event -> sendProductConfigOptionsFoundMessage(((ProductConfigOptionFound) event).getProductConfigOptions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigOptionsFoundMessage(List<ProductConfigOption> productConfigOptions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigOptions);
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
	public boolean createProductConfigOption(HttpServletRequest request) {

		ProductConfigOption productConfigOptionToBeAdded = new ProductConfigOption();
		try {
			productConfigOptionToBeAdded = ProductConfigOptionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigOption(productConfigOptionToBeAdded);

	}

	/**
	 * creates a new ProductConfigOption entry in the ofbiz database
	 * 
	 * @param productConfigOptionToBeAdded
	 *            the ProductConfigOption thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigOption(ProductConfigOption productConfigOptionToBeAdded) {

		AddProductConfigOption com = new AddProductConfigOption(productConfigOptionToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionAdded.class,
				event -> sendProductConfigOptionChangedMessage(((ProductConfigOptionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfigOption(HttpServletRequest request) {

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

		ProductConfigOption productConfigOptionToBeUpdated = new ProductConfigOption();

		try {
			productConfigOptionToBeUpdated = ProductConfigOptionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigOption(productConfigOptionToBeUpdated);

	}

	/**
	 * Updates the ProductConfigOption with the specific Id
	 * 
	 * @param productConfigOptionToBeUpdated the ProductConfigOption thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigOption(ProductConfigOption productConfigOptionToBeUpdated) {

		UpdateProductConfigOption com = new UpdateProductConfigOption(productConfigOptionToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionUpdated.class,
				event -> sendProductConfigOptionChangedMessage(((ProductConfigOptionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfigOption from the database
	 * 
	 * @param productConfigOptionId:
	 *            the id of the ProductConfigOption thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigOptionById(@RequestParam(value = "productConfigOptionId") String productConfigOptionId) {

		DeleteProductConfigOption com = new DeleteProductConfigOption(productConfigOptionId);

		int usedTicketId;

		synchronized (ProductConfigOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionDeleted.class,
				event -> sendProductConfigOptionChangedMessage(((ProductConfigOptionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigOptionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigOption/\" plus one of the following: "
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

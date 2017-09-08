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
import com.skytala.eCommerce.command.AddProductStorePromoAppl;
import com.skytala.eCommerce.command.DeleteProductStorePromoAppl;
import com.skytala.eCommerce.command.UpdateProductStorePromoAppl;
import com.skytala.eCommerce.entity.ProductStorePromoAppl;
import com.skytala.eCommerce.entity.ProductStorePromoApplMapper;
import com.skytala.eCommerce.event.ProductStorePromoApplAdded;
import com.skytala.eCommerce.event.ProductStorePromoApplDeleted;
import com.skytala.eCommerce.event.ProductStorePromoApplFound;
import com.skytala.eCommerce.event.ProductStorePromoApplUpdated;
import com.skytala.eCommerce.query.FindProductStorePromoApplsBy;

@RestController
@RequestMapping("/api/productStorePromoAppl")
public class ProductStorePromoApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStorePromoAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStorePromoApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStorePromoAppl
	 * @return a List with the ProductStorePromoAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStorePromoAppl> findProductStorePromoApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStorePromoApplsBy query = new FindProductStorePromoApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStorePromoApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePromoApplFound.class,
				event -> sendProductStorePromoApplsFoundMessage(((ProductStorePromoApplFound) event).getProductStorePromoAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStorePromoApplsFoundMessage(List<ProductStorePromoAppl> productStorePromoAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStorePromoAppls);
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
	public boolean createProductStorePromoAppl(HttpServletRequest request) {

		ProductStorePromoAppl productStorePromoApplToBeAdded = new ProductStorePromoAppl();
		try {
			productStorePromoApplToBeAdded = ProductStorePromoApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStorePromoAppl(productStorePromoApplToBeAdded);

	}

	/**
	 * creates a new ProductStorePromoAppl entry in the ofbiz database
	 * 
	 * @param productStorePromoApplToBeAdded
	 *            the ProductStorePromoAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStorePromoAppl(ProductStorePromoAppl productStorePromoApplToBeAdded) {

		AddProductStorePromoAppl com = new AddProductStorePromoAppl(productStorePromoApplToBeAdded);
		int usedTicketId;

		synchronized (ProductStorePromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePromoApplAdded.class,
				event -> sendProductStorePromoApplChangedMessage(((ProductStorePromoApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStorePromoAppl(HttpServletRequest request) {

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

		ProductStorePromoAppl productStorePromoApplToBeUpdated = new ProductStorePromoAppl();

		try {
			productStorePromoApplToBeUpdated = ProductStorePromoApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStorePromoAppl(productStorePromoApplToBeUpdated);

	}

	/**
	 * Updates the ProductStorePromoAppl with the specific Id
	 * 
	 * @param productStorePromoApplToBeUpdated the ProductStorePromoAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStorePromoAppl(ProductStorePromoAppl productStorePromoApplToBeUpdated) {

		UpdateProductStorePromoAppl com = new UpdateProductStorePromoAppl(productStorePromoApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductStorePromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePromoApplUpdated.class,
				event -> sendProductStorePromoApplChangedMessage(((ProductStorePromoApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStorePromoAppl from the database
	 * 
	 * @param productStorePromoApplId:
	 *            the id of the ProductStorePromoAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStorePromoApplById(@RequestParam(value = "productStorePromoApplId") String productStorePromoApplId) {

		DeleteProductStorePromoAppl com = new DeleteProductStorePromoAppl(productStorePromoApplId);

		int usedTicketId;

		synchronized (ProductStorePromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePromoApplDeleted.class,
				event -> sendProductStorePromoApplChangedMessage(((ProductStorePromoApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStorePromoApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStorePromoAppl/\" plus one of the following: "
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

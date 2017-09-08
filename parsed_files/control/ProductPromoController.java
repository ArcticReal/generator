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
import com.skytala.eCommerce.command.AddProductPromo;
import com.skytala.eCommerce.command.DeleteProductPromo;
import com.skytala.eCommerce.command.UpdateProductPromo;
import com.skytala.eCommerce.entity.ProductPromo;
import com.skytala.eCommerce.entity.ProductPromoMapper;
import com.skytala.eCommerce.event.ProductPromoAdded;
import com.skytala.eCommerce.event.ProductPromoDeleted;
import com.skytala.eCommerce.event.ProductPromoFound;
import com.skytala.eCommerce.event.ProductPromoUpdated;
import com.skytala.eCommerce.query.FindProductPromosBy;

@RestController
@RequestMapping("/api/productPromo")
public class ProductPromoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromo
	 * @return a List with the ProductPromos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromo> findProductPromosBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromosBy query = new FindProductPromosBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoFound.class,
				event -> sendProductPromosFoundMessage(((ProductPromoFound) event).getProductPromos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromosFoundMessage(List<ProductPromo> productPromos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromos);
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
	public boolean createProductPromo(HttpServletRequest request) {

		ProductPromo productPromoToBeAdded = new ProductPromo();
		try {
			productPromoToBeAdded = ProductPromoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromo(productPromoToBeAdded);

	}

	/**
	 * creates a new ProductPromo entry in the ofbiz database
	 * 
	 * @param productPromoToBeAdded
	 *            the ProductPromo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromo(ProductPromo productPromoToBeAdded) {

		AddProductPromo com = new AddProductPromo(productPromoToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoAdded.class,
				event -> sendProductPromoChangedMessage(((ProductPromoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromo(HttpServletRequest request) {

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

		ProductPromo productPromoToBeUpdated = new ProductPromo();

		try {
			productPromoToBeUpdated = ProductPromoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromo(productPromoToBeUpdated);

	}

	/**
	 * Updates the ProductPromo with the specific Id
	 * 
	 * @param productPromoToBeUpdated the ProductPromo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromo(ProductPromo productPromoToBeUpdated) {

		UpdateProductPromo com = new UpdateProductPromo(productPromoToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoUpdated.class,
				event -> sendProductPromoChangedMessage(((ProductPromoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromo from the database
	 * 
	 * @param productPromoId:
	 *            the id of the ProductPromo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoById(@RequestParam(value = "productPromoId") String productPromoId) {

		DeleteProductPromo com = new DeleteProductPromo(productPromoId);

		int usedTicketId;

		synchronized (ProductPromoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoDeleted.class,
				event -> sendProductPromoChangedMessage(((ProductPromoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromo/\" plus one of the following: "
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

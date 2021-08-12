/**
 * File: PersonResource.java Course materials (21S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Update by:  Ehsan
 * 
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.PHONE_RECORD_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Contact;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import bloodbank.entity.SecurityUser;

@Path(PHONE_RECORD_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
/**
 * 
 * @author Ehsan
 *
 */
public class PhoneResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE})
	public Response getPhones() {
		LOG.debug("retrieving all Phones ...");
		List<Phone> phone = service.getAllPhones();
		Response response = Response.ok(phone).build();
		return response;
	}

	@GET
	@RolesAllowed({ADMIN_ROLE, USER_ROLE})
	@Path(RESOURCE_PATH_ID_PATH)
	public Response getPhoneWithID(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("try to retrieve specific Phone " + id);
		Response response = null;
		Phone phone = null;

		if (sc.isCallerInRole(ADMIN_ROLE) ) {
			phone = service.getPhoneByID(id);
			response = Response.status(phone == null ? Status.NOT_FOUND : Status.OK).entity(phone).build();
		}
		else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			Person user = sUser.getPerson();
			if ( user != null) {			
				for (Contact c : user.getContacts()) {
					if (c.getPhone().getId()==id) {
						response = Response.status( Status.OK).entity( phone).build();
					}
				}
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		}
		
		 else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addPhoneRecord(Phone newPhone) {
		Phone phoneRecord= service.persistPhone( newPhone);
		Response response = Response.ok(phoneRecord).build();
		return response;
	}
	
	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{id}")
	public Response updatePhoneRecord( @PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Phone recordWithUpdates) {
		Phone updatedPhone = service.updatePhone(id, recordWithUpdates);
		Response response = Response.ok( updatedPhone).build();
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{id}")
	public Response deletePhoneRecord( @PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		service.deletePhoneById(id);
		Response response = Response.ok().build();
		return response;
	}
	
	
	
}
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.BLOOD_DONATION_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;

@Path(BLOOD_DONATION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class BloodDonationResource {
	
	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;
	

	@GET
	//emily edit @get: Any user can retrieve the list of BloodDonations and BloodBanks. 
	public Response getBloodDonationRecord() {
		LOG.debug( "retrieving all blood donations");
		List<BloodDonation> bloodDonation = service.getAllBloodDonation();
		Response response = Response.ok( bloodDonation).build();
		return response;
	}

	 
	@GET
	@Path("{bloodDonationID}")
	public Response getBloodDonationById( @PathParam( "bloodDonationID") int id) {
		LOG.debug( "try to retrieve specific blood donation with id:  " + id);
		BloodDonation bloodDonation = service.getBloodDonationbyId(id);
		Response response = Response.ok(bloodDonation).build();
		return response;
	}
	
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{bloodBankId}")
	//Nouran Nouh edit @Post so it takes care of dependencies
	public Response addBloodDonation( @PathParam("bloodBankId") int id,BloodDonation newBloodDonation) {
		Response response = null;
		BloodDonation bloodDonation= service.persistBloodDonation(id,newBloodDonation);
		response = Response.ok(bloodDonation).build();
		return response;
	}
	
	
	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{bloodDonationID}/bloodbank/{bloodBankId}")
	//Nouran Nouh edit @Put so it takes care of dependencies
	public Response updateBloodDonation( @PathParam("bloodDonationID") int id,@PathParam("bloodBankId") int bloodBankId, BloodDonation recordWithUpdates) {
		BloodDonation updatedBloodDonation = service.updateBloodDonation(id,bloodBankId ,recordWithUpdates);
		Response response = Response.ok( updatedBloodDonation).build();
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{bloodDonationID}")
	public Response deleteBloodDonation( @PathParam("bloodDonationID") int id) {
		service.deleteBloodDonationbyId(id);
		Response response = Response.ok().build();
		return response;
	}
	
	

}

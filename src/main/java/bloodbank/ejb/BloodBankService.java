/**
 * File: RecordService.java
 * Course materials (21S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 *
 * Updated by:  Nouran Nouh, add CRUD Functionality to DonationRecord, BloodBank, Blood Donation and did DeleteAddressById() for method
 *
 */
package bloodbank.ejb;

import static bloodbank.entity.BloodBank.ALL_BLOODBANKS_QUERY_NAME;
import static bloodbank.entity.BloodBank.IS_DUPLICATE_QUERY_NAME;
import static bloodbank.entity.Person.ALL_PERSONS_QUERY_NAME;
import static bloodbank.entity.SecurityRole.ROLE_BY_NAME_QUERY;
import static bloodbank.entity.SecurityUser.USER_FOR_OWNING_PERSON_QUERY;
import static bloodbank.utility.MyConstants.DEFAULT_KEY_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.DEFAULT_SALT_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PARAM1;
import static bloodbank.utility.MyConstants.PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.PROPERTY_KEYSIZE;
import static bloodbank.utility.MyConstants.PROPERTY_SALTSIZE;
import static bloodbank.utility.MyConstants.PU_NAME;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import bloodbank.entity.Address;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.Contact;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import bloodbank.entity.SecurityRole;
import bloodbank.entity.SecurityUser;


/**
 * Stateless Singleton ejb Bean - BloodBankService
 */
@Singleton
public class BloodBankService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();



    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Person> getAllPeople() {
    	//Added the following code
//    	CriteriaBuilder cb = em.getCriteriaBuilder();
//    	CriteriaQuery<Person> cq = cb.createQuery(Person.class);
//    	cq.select(cq.from(Person.class));
//    	return em.createQuery(cq).getResultList();
    	TypedQuery< Person> allPeopleQuery = em.createNamedQuery( Person.ALL_PERSONS_QUERY_NAME, Person.class);
		return allPeopleQuery.getResultList();
    	//return null;
    }

    public Person getPersonId(int id) {
    	return em.find(Person.class, id);
    	//return null;
    }

    @Transactional
    public Person persistPerson(Person newPerson) {
    	em.persist(newPerson);
    	return newPerson;
    }

    @Transactional
    public void buildUserForNewPerson(Person newPerson) {
        SecurityUser userForNewPerson = new SecurityUser();
        userForNewPerson.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPerson.getFirstName() + "." + newPerson.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALTSIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEYSIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPerson.setPwHash(pwHash);
        userForNewPerson.setPerson(newPerson);
        SecurityRole userRole = em.createNamedQuery(ROLE_BY_NAME_QUERY, SecurityRole.class)
            .setParameter(PARAM1, USER_ROLE).getSingleResult();
        userForNewPerson.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPerson);
        em.persist(userForNewPerson);
    }

    //set Address contact
    @Transactional
    public Person setAddressFor(int id, Address newAddress) {
    	Person updatePerson = getPersonId(id);

    	for (Contact c : updatePerson.getContacts()) {
    		c.setAddress(newAddress);
    	}

    	return updatePerson;
    }


    @Transactional
    public Address setAddressForPersonPhone(int personId, int phoneId, Address newAddress) {
    	Person personToBeUpdated = em.find(Person.class, personId);
    	if (personToBeUpdated != null) { // Person exists
    		Set<Contact> contacts = personToBeUpdated.getContacts();
    		contacts.forEach(c -> {
    			if (c.getPhone().getId() == phoneId) {
    				if (c.getAddress() != null) { // Address exists
    					Address addr = em.find(Address.class, c.getAddress().getId());
    					addr.setAddress(newAddress.getStreetNumber(),
    							        newAddress.getStreet(),
    							        newAddress.getCity(),
    							        newAddress.getProvince(),
    							        newAddress.getCountry(),
    							        newAddress.getZipcode());
    					em.merge(addr);

    				}
    				else { // Address does not exist
    					c.setAddress(newAddress);
    					em.merge(personToBeUpdated);
    				}
    			}
    		});
    		return newAddress;
    	}
    	else // Person doesn't exist
    		return null;
    }

    /**
     * to update a person
     *
     * @param id - id of entity to update
     * @param personWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Person updatePersonById(int id, Person personWithUpdates) {
        Person personToBeUpdated = getPersonId(id);
        if (personToBeUpdated != null) {
            em.refresh(personToBeUpdated);
            em.merge(personWithUpdates);
            em.flush();
        }
        return personToBeUpdated;
    }

    /**
     * to delete a person by id
     *
     * @param id - person id to delete
     */
    @Transactional
    public void deletePersonById(int id) {
        Person person = getPersonId(id);
        if (person != null) {
            em.refresh(person);
            TypedQuery<SecurityUser> findUser = em
                .createNamedQuery(USER_FOR_OWNING_PERSON_QUERY, SecurityUser.class)
                .setParameter(PARAM1, person.getId());
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(person);
        }
    }

    /**********************************************************************************************************************
     *
     * 													DonationRecord functionality
     *
     ***********************************************************************************************************************/
    /**
     * to list all donation record
     * @return list of all donation record
     */

    public List<DonationRecord> getAllDonationRecord(){
    	TypedQuery< DonationRecord> allRecordsQuery = em.createNamedQuery( DonationRecord.ALL_RECORDS_QUERY_NAME, DonationRecord.class);
    	return allRecordsQuery.getResultList();
    }

    /**
     * find specific donation record by id
     *
     * @param id - id of the record to find
     * @return Entity associated with the id
     */

    public DonationRecord getDonationRecordById(int id) {
    	return em.find(DonationRecord.class, id);
    }

    /**
     * add a new donation record
     *
     * @param newDonationRecord - Entity to add to the table
     * @return the entity that was added to the table
     */
    @Transactional
    public DonationRecord persistDonationRecord(int id,int bloodDonationId,DonationRecord newDonationRecord) {
    	//find person
    	//call donation record to person entity
    	//add new Person blood donation to blood record

 	    Person updatePerson = getPersonId(id);
 	   BloodDonation donation=getBloodDonationbyId(bloodDonationId);


 	    newDonationRecord.setOwner(updatePerson);


 	   // updatePerson.getDonations().add(newDonationRecord);

	    if( donation.getId()!=id)
	    	newDonationRecord.setDonation(donation);

 	  //  UpdateBloodDonation(bloodDonationID, donation);




    	em.persist(newDonationRecord);
    	return newDonationRecord;
    }

    /**
     * update donation record
     *
     * @param id to update
     * @param donationRecordWithUpdates - Entity with updates to provide
     * @return the updated donation record
     */
    @Transactional
    public DonationRecord updateDonationRecord(int id,int personId,DonationRecord updatedDonationRecord) {


    	DonationRecord recordToUpdate = getDonationRecordById(id);


        	Person updatePerson = getPersonId(personId);


            em.refresh(recordToUpdate);
            //Person p=   recordToUpdate.getOwner();
            updatedDonationRecord.setOwner(updatePerson);

          //   updatePerson.getDonations().add(updatedDonationRecord);
           //Set<DonationRecord> d= updatePerson.getDonations();
           if(recordToUpdate !=null) {
        	   recordToUpdate.setOwner(updatedDonationRecord.getOwner());
        	   recordToUpdate.setTested(updatedDonationRecord.getTested());
        	   em.merge(recordToUpdate);
           }

            //updatedDonationRecord.set



           // em.remove(recordToUpdate);
            em.flush();



        return recordToUpdate;

    }

    /**
     * delete a donation record
     *
     * @param id - id of the record to delete
     */
    @Transactional
    public void deleteDonationRecordById(int id) {
    	DonationRecord record = getDonationRecordById(id);

        if (record != null) {
        	em.refresh(record);
        	em.remove(record);



        }
    }

    /**********************************************************************************************************************
     *
     * 													BloodDonation functionality
     *
     ***********************************************************************************************************************/
    /**
     * to list all blood donation
     * @return list of all blood donation
     */

    public List<BloodDonation> getAllBloodDonation(){
    	TypedQuery< BloodDonation> allRecordsQuery = em.createNamedQuery( BloodDonation.FIND_ALL,BloodDonation.class);
    	return allRecordsQuery.getResultList();
    }

    /**
     * find specific blood donation by id
     *
     * @param id - id of the blood donation
     * @return Entity associated with the id
     */

    public BloodDonation getBloodDonationbyId(int id) {
    	return em.find(BloodDonation.class, id);
    }

    /**
     * add a new blood donation
     *
     * @param newBloodDonation - Entity to add to the table
     * @return the entity that was added to the table
     */
    @Transactional
    public BloodDonation persistBloodDonation(int id,BloodDonation newBloodDonation) {
    	BloodBank b=getBloodBankById(id);
    	newBloodDonation.setBank(b);
    	em.persist(newBloodDonation);
    	return newBloodDonation;
    }

    /**
     * update blood donation record
     *
     * @param id to update
     * @param donationRecordWithUpdates - Entity with updates to provide
     * @return the updated blood donation
     */
    @Transactional
    public BloodDonation updateBloodDonation(int id,int bloodBankId,BloodDonation updatedBloodDonation) {

    	BloodDonation recordToUpdate = getBloodDonationbyId(id);
    	BloodBank bb= getBloodBankById(bloodBankId);
    	em.refresh(recordToUpdate);
    	updatedBloodDonation.setBank(bb);
        if (recordToUpdate != null && recordToUpdate.getId()==id) {
		       recordToUpdate.setBank(updatedBloodDonation.getBank());
		       recordToUpdate.setBloodType(updatedBloodDonation.getBloodType());
		       recordToUpdate.setMilliliters(updatedBloodDonation.getMilliliters());
		       em.merge(recordToUpdate);

        }
        em.flush();
        return recordToUpdate;

    }

    /**
     * delete a blood donation and reference to donationRecord records
     *
     * @param id - id of the record to delete
     */
    @Transactional
    public void deleteBloodDonationbyId(int id) {
    	BloodDonation record = getBloodDonationbyId(id);
        if (record != null) {
        	em.refresh(record);
        	TypedQuery<DonationRecord> findDonationRecord=em.createNamedQuery(DonationRecord.ID_RECORD_QUERY_NAME, DonationRecord.class)
        			.setParameter(PARAM1,record.getId());

       	   DonationRecord d=findDonationRecord.getSingleResult();


       	    em.remove(d);
        	em.remove(record);



        }
    }

    /**********************************************************************************************************************
     *
     * 													BloodBank functionality
     *
     ***********************************************************************************************************************/

    /**
     * to list all blood banks
     *
     * @return list of all blood banks
     */

    public List<BloodBank> getAllBloodBanks() {

    	CriteriaBuilder cb = em.getCriteriaBuilder();
    	CriteriaQuery<BloodBank> cq = cb.createQuery(BloodBank.class);
    	cq.select(cq.from(BloodBank.class));
    	return em.createQuery(cq).getResultList();
    }

    /**
     * find specific blood bank by id
     *
     * @param id - id of the record to find
     * @return Entity associated with the id
     **/

    public BloodBank getBloodBankById(int bloodBankId) {

    	return em.find(BloodBank.class, bloodBankId);
    }

    /**
     * add a new blood bank
     *
     * @param newBloodBank - to add to the table
     * @return the entity that was added to the table
     */
    @Transactional
    public BloodBank persistBloodBank(BloodBank newBloodBank) {

    	em.persist(newBloodBank);
    	return newBloodBank;
    }

    /**
     * update a blood bank by id
     *
     * @param id - id of the record to update
     * @param bloodBankWithUpdates - Entity with updates to provide
     * @return the updated record
     */
    @Transactional
    public BloodBank updateBloodBankById(int id, BloodBank bloodBankUpdated) {
    	BloodBank bankToUpdate = getBloodBankById(id);
        if (bankToUpdate != null) {

        	em.refresh(bankToUpdate);
            em.merge(bloodBankUpdated);
            em.flush();
        }

        return bankToUpdate;
    }

    /**
     * delete a blood bank by id and referenced bloodDonation
     *
     * @param id - id of the record to delete
     */
    @Transactional
    public BloodBank deleteBloodBankById(int id) {
    	BloodBank bb= getBloodBankById(id);
    	Set<BloodDonation> donations = bb.getDonations();

    	// You can either delete using a new named query and delete all blood donations with the specified blood bank id (id)
    	// Or you can loop through the list and manually remove them as I am doing below

    	List<BloodDonation> list = new LinkedList<>();
    	donations.forEach(list::add);

    	list.forEach(bd -> {
    		if (bd.getRecord() != null) {
    			DonationRecord dr = getById(DonationRecord.class, DonationRecord.ID_RECORD_QUERY_NAME, bd.getRecord().getId());
    			dr.setDonation(null);
    		}
    		bd.setRecord(null);
    		em.merge(bd);
    	});

    	em.remove(bb);
    	return bb;
    }



    // Please try to understand and test the below:
   public boolean isDuplicated(BloodBank newBloodBank) {
       TypedQuery<Long> allBloodBankQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
       allBloodBankQuery.setParameter(PARAM1, newBloodBank.getName());
       return (allBloodBankQuery.getSingleResult() >= 1);
	    }



    /**********************************************************************************************************************
     *
     * 													Phone functionality			Muhammad Ehsan Khan
     *
     ***********************************************************************************************************************/

    /**
     * to list all hones
     *
     * @return list of all Addresses
     */
    public List<Phone> getAllPhones() {

    	TypedQuery< Phone> allPhoneQuery = em.createNamedQuery( Phone.ALL_RECORDS_QUERY_NAME, Phone.class);
    	return allPhoneQuery.getResultList();
    }

    /**
     * find specific Address by id
     *
     * @param id - id of the record to find
     * @return Entity associated with the id
     */
    public Phone getPhoneByID(int id) {

    	return em.find(Phone.class, id);
    }

    /**
     * add a new Phone
     *
     * @param newPhone - to add to the table
     * @return the entity that was added to the table
     */
    @Transactional
    public Phone persistPhone(Phone newPhone) {

    	em.persist(newPhone);
    	return newPhone;
    }

    /**
     * update an Address by id
     *
     * @param id - id of the record to update
     * @param PhoneWithUpdates - Entity with updates to provide
     * @return the updated record
     */
    @Transactional
    public Phone updatePhone(int id, Phone PhoneWithUpdates) {
    	Phone phoneToUpdate = getPhoneByID(id);
        if (phoneToUpdate != null) {

        	em.refresh(phoneToUpdate);
            em.merge(PhoneWithUpdates);
            em.flush();
        }

        return phoneToUpdate;
    }

    /**
     * delete a blood bank by id and referenced bloodDonation
     *
     * @param id - id of the record to delete
     */
    @Transactional
    public void deletePhoneById(int id) {
    	Phone record = getPhoneByID(id);
        if (record != null) {
        	Set <Contact> c = record.getContacts();
        	for (Contact contact : c) {
				contact.setAddress(null);
			}
        	em.remove(record);
        }
    }

    /**********************************************************************************************************************
     *
     * 													Address functionality		Muhammad Ehsan Khan
     *
     ***********************************************************************************************************************/

    /**
     * to list all Addresses
     *
     * @return list of all Addresses
     */
    public List<Address> getAllAddresses() {

    	TypedQuery< Address> allAddressQuery = em.createNamedQuery( Address.ALL_RECORDS_QUERY_NAME, Address.class);
    	return allAddressQuery.getResultList();
    }

    /**
     * find specific Address by id
     *
     * @param id - id of the record to find
     * @return Entity associated with the id
     */
    public Address getAddressByID(int id) {

    	return em.find(Address.class, id);
    }

    /**
     * add a new Address
     *
     * @param newAddress - to add to the table
     * @return the entity that was added to the table
     */
    @Transactional
    public Address persistAddress(Address newAddress) {
    	em.persist(newAddress);
    	return newAddress;
    }

    /**
     * update an Address by id
     *
     * @param id - id of the record to update
     * @param AddressWithUpdates - Entity with updates to provide
     * @return the updated record
     */
    @Transactional
    public Address updateAddressById(int id, Address AddressWithUpdates) {
    	Address addressToUpdate = getAddressByID(id);
        if (addressToUpdate != null) {

        	em.refresh(addressToUpdate);
            em.merge(AddressWithUpdates);
            em.flush();
        }

        return addressToUpdate;
    }

    /**
     * delete a address by id and referenced bloodDonation
     *
     * @param id - id of the record to delete
     */
    @Transactional
    public void deleteAddressById(int id) {
    	Address record = getAddressByID(id);
    	Set<Contact> contact=record.getContacts();
    	for(Contact c:contact) {
    		c.setAddress(null);

    	}

    	em.remove(record);



    }


    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
    	TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
    	return allQuery.getResultList();
    }

    public <T> T getById(Class<T> entity, String namedQuery, int id) {
    	TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
    	allQuery.setParameter(PARAM1, id);
    	return allQuery.getSingleResult();
    }


}

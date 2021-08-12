package bloodbank.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the donation_record database table.
 * 
 */
@Entity
@Table( name = "donation_record")
@NamedQuery( name = DonationRecord.ALL_RECORDS_QUERY_NAME, query = "SELECT d FROM DonationRecord d left join fetch d.donation left join fetch d.owner")
@NamedQuery( name = DonationRecord.ID_RECORD_QUERY_NAME, query = "SELECT d FROM DonationRecord d left join fetch d.donation left join fetch d.owner where d.donation.id=:param1")
@AttributeOverride( name = "id", column = @Column( name = "record_id"))
public class DonationRecord extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ALL_RECORDS_QUERY_NAME = "DonationRecord.findAll";
	public static final String ID_RECORD_QUERY_NAME = "DonationRecord.findByID";

	@OneToOne( fetch = FetchType.LAZY, optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn( name = "donation_id", referencedColumnName = "donation_id")
	private BloodDonation donation;
   
	@ManyToOne( cascade = {CascadeType.MERGE, CascadeType.REFRESH},fetch = FetchType.LAZY, optional = false)
	@JoinColumn( name = "person_id", referencedColumnName = "id", nullable = false)
	private Person owner;

	private byte tested;

	public DonationRecord() {

	}

	@JsonIgnore
	public BloodDonation getDonation() {
		return donation;
	}

	public void setDonation( BloodDonation donation) {
		this.donation = donation;
	}

	@JsonIgnore
	public Person getOwner() {
		return owner;
	}

	public void setOwner( Person owner) {
		this.owner = owner;
	}

	public byte getTested() {
		return tested;
	}

	public void setTested( byte tested) {
		this.tested = tested;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + Objects.hash( getId(), getOwner().getId(), getTested());
	}

	@Override
	public boolean equals( Object obj) {
		if ( obj == null)
			return false;
		if ( this == obj)
			return true;
		if ( !( getClass() == obj.getClass() || Hibernate.getClass( obj) == getClass()))
			return false;
		DonationRecord other = (DonationRecord) obj;
		return Objects.equals( getId(), other.getId()) && Objects.equals( getOwner().getId(), other.getOwner().getId())
				&& getTested() == other.getTested();
	}

}
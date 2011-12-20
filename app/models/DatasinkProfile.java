package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * A DatasinkProfile is a blueprint for a Datasink instance. 
 *  
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Entity
public class DatasinkProfile extends Model {
	
	@ManyToOne
	public User user;

	public String name;

	public String implementationClass;
	
	public DatasinkProfile(User user, String name, String implementationClass) {
		this.user = user;
		this.name = name;
		this.implementationClass = implementationClass;
	}

}

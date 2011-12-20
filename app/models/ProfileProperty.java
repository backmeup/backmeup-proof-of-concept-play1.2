package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * A property of a {@link DatasinkProfile} or {@link DatasourceProfile}.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Entity
public class ProfileProperty extends Model {
	
	@ManyToOne
	public DatasourceProfile profile;

	public String propertyName;
	
	public String propertyValue;
	
	ProfileProperty(DatasourceProfile profile, String propertyName, String propertyValue) {
		this.profile = profile;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
}

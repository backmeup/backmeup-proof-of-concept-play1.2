package models;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

/**
 * A DatasourceProfile is a blueprint for a Datasource instance. 
 *  
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Entity
public class DatasourceProfile extends Model {
	
	@ManyToOne
	public User user;

	public String name;

	public String implementationClass;
	
	DatasourceProfile(User user, String name, String implementationClass) {
		this.user = user;
		this.name = name;
		this.implementationClass = implementationClass;
	}
	
	public List<ProfileProperty> getProperties() {
		return ProfileProperty.find("byProfile", this).fetch();
	}
	
	public ProfileProperty getProperty(String propertyName) {
		return ProfileProperty.find("byProfileAndPropertyName", this, propertyName).first();
	}
	
	public ProfileProperty setProperty(String propertyName, String propertyValue) {
		// Remove if exists
		ProfileProperty old = getProperty(propertyName);
		if (old != null)
			old.delete();
		
		ProfileProperty property = new ProfileProperty(this, propertyName, propertyValue);
		property.save();
		return property;
	}
	
	@Override
	public DatasourceProfile delete() {
		for (ProfileProperty p : getProperties()) {
			p.delete();
		}
		
		List<Workflow> workflows = Workflow.find("bySourceProfile", this).fetch(); 
		for (Workflow w : workflows) {
			w.delete();
		}
		
		return super.delete();
	}

}

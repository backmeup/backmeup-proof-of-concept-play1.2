package models;

import java.io.IOException;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.backmeup.storage.hdfs.util.HdfsUtil;

import play.db.jpa.Model;

@Entity
public class UserDownload extends Model {
	
	@ManyToOne
	public User owner;

	public String path;

	public Date creationDate;
	
	UserDownload(User owner, String path, Date creationDate) {
		this.owner = owner;
		this.path = path;
		this.creationDate = creationDate;
	}
	
	@Override
	public UserDownload delete() {
		HdfsUtil util = new HdfsUtil();
		try {
			util.open();
			util.deleteFile(this.path);
			return super.delete();
		} catch (IOException e) {
			System.out.println("Unable to delete from HDFS: " + this.path);
			e.printStackTrace();
		}
		return this;
	}

}

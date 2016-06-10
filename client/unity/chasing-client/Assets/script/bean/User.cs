using System;

public class User
{
	public int id { get; set;}
	public string password { get; set; }

	public User(int id, string password) {
		this.id = id;
		this.password = password;
	}
}
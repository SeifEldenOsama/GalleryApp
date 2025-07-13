package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;

public class dataBaseConnection {
	public static Connection connect() {
	Connection con = null;
	try {
		con = DriverManager.getConnection("jdbc:mysql://localhost/Gallery?user=root&password=***********");
		System.out.println("Success");
		return con;
	}catch(Exception E) {
		System.out.println(E.getMessage());
		return con;
	}
}
}

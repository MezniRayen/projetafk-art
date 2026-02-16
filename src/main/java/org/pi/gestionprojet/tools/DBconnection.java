package org.pi.gestionprojet.tools;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class  DBconnection {
    public String url="jdbc:mysql://localhost:3306/mydb" ;

    public String login="root";
    public String pwd="" ;
    Connection cnx ;
    public static DBconnection instance ;
    private DBconnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connection établie");
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
    public Connection getCnx(){
        return cnx ;
    }

    public static DBconnection getInstance(){
        if (instance==null){
            instance=new DBconnection() ;
        }
        return instance ;



    }
}
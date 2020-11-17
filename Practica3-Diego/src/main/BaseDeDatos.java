package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDeDatos {
   private static Connection connection = null;
   private static Statement statement = null;

   public static Connection initBD(String nombreBD) {
      try {
         Class.forName("org.sqlite.JDBC");
         connection = DriverManager.getConnection("jdbc:sqlite:" + nombreBD);
         statement = connection.createStatement();
         statement.setQueryTimeout(30);
         return connection;
      } catch (SQLException | ClassNotFoundException var2) {
         return null;
      }
   }

   public static void close() {
      try {
         statement.close();
         connection.close();
      } catch (SQLException var1) {
         var1.printStackTrace();
      }

   }

   public static Connection getConnection() {
      return connection;
   }

   public static Statement getStatement() {
      return statement;
   }

   public static void crearTablaBD() {
      if (statement != null) {
         try {
            statement.executeUpdate("create table fichero_multimedia (fichero string, error boolean, titulo string, cantante string, comentarios string)");
         } catch (SQLException var1) {
         }

      }
   }
}

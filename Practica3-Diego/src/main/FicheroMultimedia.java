package main;

import java.io.File;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class FicheroMultimedia implements Serializable {
   private static final long serialVersionUID = 1L;
   public File file;
   public boolean erroneo;
   public String titulo;
   public String cantante;
   public String comentarios;

   public FicheroMultimedia(File file, boolean erroneo, String titulo, String cantante, String comentarios) {
      this.file = file;
      this.erroneo = erroneo;
      this.titulo = titulo == null ? "" : titulo;
      this.cantante = cantante == null ? "" : cantante;
      this.comentarios = comentarios == null ? "" : comentarios;
   }

   public FicheroMultimedia(File file) {
      this(file, false, "", "", "");
   }

   public boolean anyadirFilaATabla(Statement st) {
      if (this.chequearYaEnTabla(st)) {
         return this.modificarFilaEnTabla(st);
      } else {
         try {
            String sentSQL = "insert into fichero_multimedia values('" + this.file.getAbsolutePath() + "', " + "'" + this.erroneo + "', " + "'" + this.titulo + "', " + "'" + this.cantante + "', " + "'" + this.comentarios + "')";
            System.out.println(sentSQL);
            int val = st.executeUpdate(sentSQL);
            return val == 1;
         } catch (SQLException var4) {
            var4.printStackTrace();
            return false;
         }
      }
   }

   public boolean chequearYaEnTabla(Statement st) {
      try {
         String sentSQL = "select * from fichero_multimedia where (fichero = '" + this.file.getAbsolutePath() + "')";
         System.out.println(sentSQL);
         ResultSet rs = st.executeQuery(sentSQL);
         if (rs.next()) {
            rs.close();
            return true;
         } else {
            return false;
         }
      } catch (SQLException var4) {
         var4.printStackTrace();
         return false;
      }
   }

   public boolean modificarFilaEnTabla(Statement st) {
      try {
         String sentSQL = "update fichero_multimedia set error = '" + this.erroneo + "', " + "titulo = '" + this.titulo + "', " + "cantante = '" + this.cantante + "', " + "comentarios = '" + this.comentarios + "' " + "where (fichero = '" + this.file.getAbsolutePath() + "')";
         System.out.println(sentSQL);
         int val = st.executeUpdate(sentSQL);
         return val == 1;
      } catch (SQLException var4) {
         var4.printStackTrace();
         return false;
      }
   }

   public void cargarDeTabla(Statement st) {
      try {
         String sentSQL = "select * from fichero_multimedia where (fichero = '" + this.file.getAbsolutePath() + "')";
         System.out.println(sentSQL);
         ResultSet rs = st.executeQuery(sentSQL);
         if (rs.next()) {
            this.erroneo = rs.getBoolean("error");
            this.titulo = rs.getString("titulo");
            this.cantante = rs.getString("cantante");
            this.comentarios = rs.getString("comentarios");
            rs.close();
         }
      } catch (SQLException var4) {
         var4.printStackTrace();
      }

   }

   public static FicheroMultimedia cargarDeTabla(Statement st, String nombreFichero) {
      try {
         String sentSQL = "select * from fichero_multimedia where (fichero = '" + nombreFichero + "')";
         System.out.println(sentSQL);
         ResultSet rs = st.executeQuery(sentSQL);
         if (rs.next()) {
            FicheroMultimedia fm = new FicheroMultimedia(new File(nombreFichero));
            fm.erroneo = rs.getBoolean("error");
            fm.titulo = rs.getString("titulo");
            fm.cantante = rs.getString("cantante");
            fm.comentarios = rs.getString("comentarios");
            rs.close();
            return fm;
         } else {
            return null;
         }
      } catch (SQLException var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public static ArrayList<FicheroMultimedia> cargarVariosDeTabla(Statement st, String exprWhere) {
      try {
         ArrayList<FicheroMultimedia> lista = new ArrayList();
         String sentSQL = "select * from fichero_multimedia" + (exprWhere != null && !exprWhere.equals("") ? " where " + exprWhere : "");
         System.out.println(sentSQL);
         ResultSet rs = st.executeQuery(sentSQL);

         while(rs.next()) {
            FicheroMultimedia fm = new FicheroMultimedia(new File(rs.getString("fichero_multimedia")));
            fm.erroneo = rs.getBoolean("error");
            fm.titulo = rs.getString("titulo");
            fm.cantante = rs.getString("cantante");
            fm.comentarios = rs.getString("comentarios");
            rs.close();
            lista.add(fm);
         }

         return lista;
      } catch (SQLException var6) {
         var6.printStackTrace();
         return null;
      }
   }
}

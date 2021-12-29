import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import redis.clients.jedis.Jedis;

@WebServlet(urlPatterns = "/GetNotePadListPlus")
public class GetNotePadListPlus extends HttpServlet {
   final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
   final static String URL = "jdbc:mysql://115.227.20.2/linux_final";
   final static String USER = "root";
   final static String PASS = "Zxm20011003*";
   final static String SQL_QURERY_ALL_NOTEPAD= "SELECT * FROM t_notepad;";
   final static  String REDIS_URL = "127.0.0.1";
   
   static Connection conn = null;
   static Jedis jedis = null;

   public void init() {
      try {
         Class.forName(JDBC_DRIVER);
         conn = DriverManager.getConnection(URL, USER, PASS);
		 jedis = new Jedis(REDIS_URL);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void destroy() {
      try {
         conn.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();

      List<Notepad> noList = getAllNotepad();
 
      Gson gson = new Gson();
      String json = gson.toJson(noList, new TypeToken<List<Notepad>>() {
      }.getType());
	   for(int i=0;i<noList.size();i++){
		  jedis.set(String.valueOf(noList.get(i).id), json);
	  jedis.set(noList.get(i).notepadContent, json);
	  jedis.set(noList.get(i).notepadTime, json);
	  }      out.println(json);
      out.flush();
      out.close();
   }

   private List<Notepad> getAllNotepad() {
      List<Notepad> noList = new ArrayList<Notepad>();
      Statement stmt = null;
      try {
         stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(SQL_QURERY_ALL_NOTEPAD);
         while (rs.next()) {
            Notepad no = new Notepad();
            no.id = rs.getInt("id");
            no.notepadContent = rs.getString("notepadContent");
            no.notepadTime = rs.getString("notepadTime");
            noList.add(no);
         }
         rs.close();
         stmt.close();
      } catch (SQLException se) {
         se.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if (stmt != null)
               stmt.close();
         } catch (SQLException se) {
            se.printStackTrace();
         }
      }

      return noList;
   }
}

class Notepad {
    int id;
    String notepadContent;
    String notepadTime;

    @Override
    public String toString() {
        return "Notepad [id=" + id + ", notepadContent=" + notepadContent + ", notepadTime=" + notepadTime + "]";
    }
}

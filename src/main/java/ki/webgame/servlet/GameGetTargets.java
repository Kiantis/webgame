package ki.webgame.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ki.webgame.DBQuery;
import ki.webgame.JSONBuilder;

@WebServlet("/gettargets")
public class GameGetTargets extends HttpServlet
{
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        if(!IsLoggedIn.isLoggedIn(request))
        {
            response.setStatus(401);
            return;
        }
        
        String username = (String) request.getSession().getAttribute(Login.SES_ATT_USERNAME);
        try
        {
            new DBQuery("select u.username, u.land, "
                    + "(select u2.username from attack_history "
                    + "left join users u2 on attacking_userid = u2.id "
                    + "where defending_userid = (select id from users where username = u.username) "
                    + "order by attack_time desc limit 1), "
                    + "u.race, u.score "
                + "from users u where u.username <> ? and land >= 0.1 and registered = 1 order by land desc")
                .addParameter(username)
                .execute((ResultSet rs) ->
                {
                    response.setContentType("text/json");
                    JSONBuilder jb = new JSONBuilder();
                    jb.beginArray();
                    while (rs.next())
                    {
                        jb.beginObject();
                        jb.property("username", rs.getString(1));
                        jb.property("land", rs.getString(2));
                        jb.property("lastAttacker", rs.getString(3));
                        jb.property("race", rs.getString(4));
                        jb.property("score", rs.getLong(5));
                        jb.endObject();
                    }
                    jb.endArray();
                    response.getWriter().write(jb.toString());
                });
        }
        catch (ServletException | IOException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServletException(ex.getMessage(), ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}

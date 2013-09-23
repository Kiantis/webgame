package ki.webgame.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ki.webgame.DBQuery;
import ki.webgame.GameEngine;
import ki.webgame.JSONBuilder;

/**
 * To be called not less than 30 seconds per player.
 */
@WebServlet("/getstats")
public class GameGetStats extends HttpServlet
{
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
            GameEngine.checkHourly();
            
            new DBQuery("select score, strength, land, energy, rage, task, lastupdate from users where username = ?")
                .addParameter(username)
                .execute((ResultSet rs) ->
            {
                if (rs.next())
                {
                    JSONBuilder jb = new JSONBuilder();
                    jb.beginObject();
                    jb.property("username", username);
                    jb.property("score", rs.getLong(1));
                    jb.property("strength", rs.getDouble(2));
                    jb.property("land", rs.getDouble(3));
                    jb.property("energy", rs.getDouble(4));
                    jb.property("rage", rs.getDouble(5));
                    jb.property("task", rs.getString(6));
                    jb.property("lastupdate", rs.getTimestamp(7).getTime());
                    jb.endObject();
                    response.setContentType("text/json");
                    response.getWriter().write(jb.toString());
                }
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

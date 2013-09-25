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

@WebServlet("/getattackhistory")
public class GameGetAttackHistory extends HttpServlet
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
            new DBQuery("select "
                + "attack_time, "
                + "result, "
                + "attacker_strength_delta, "
                + "attacker_land_delta, "
                + "attacker_energy_delta, "
                + "defender_strength_delta, "
                + "defender_land_delta, "
                + "defender_energy_delta, "
                + "(select username from users where id = attacking_userid) as attacking_user, "
                + "(select username from users where id = defending_userid) as defending_user "
                + "from attack_history "
                + "where attacking_userid = (select id from users where username = ?) "
                + "or defending_userid = (select id from users where username = ?) "
                + "order by attack_time desc "
                + "limit 100")
                .addParameter(username)
                .addParameter(username)
                .execute((ResultSet rs) ->
            {
                JSONBuilder jb = new JSONBuilder();
                jb.beginArray();
                while (rs.next())
                {
                    int i = 1;
                    jb.beginObject();
                    jb.property("time", rs.getTimestamp(i++).getTime());
                    jb.property("result", rs.getDouble(i++));
                    jb.property("deltaStrengthA", rs.getDouble(i++));
                    jb.property("deltaLandA", rs.getDouble(i++));
                    jb.property("deltaEnergyA", rs.getDouble(i++));
                    jb.property("deltaStrengthD", rs.getDouble(i++));
                    jb.property("deltaLandD", rs.getDouble(i++));
                    jb.property("deltaEnergyD", rs.getDouble(i++));
                    String attacker =  rs.getString(i++);
                    jb.property("attacker", attacker);
                    jb.property("defender", rs.getString(i++));
                    jb.property("isAttacker", attacker.equals(username));
                    jb.endObject();
                }
                jb.endArray();
                response.setContentType("text/json");
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

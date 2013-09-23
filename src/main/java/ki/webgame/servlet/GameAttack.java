package ki.webgame.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ki.webgame.GameEngine;
import ki.webgame.JSONBuilder;

@WebServlet("/attack")
public class GameAttack extends HttpServlet
{
    private static final String REQ_PAR_ATTACKED_USER = "attackeduser";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        if(!IsLoggedIn.isLoggedIn(request))
        {
            response.setStatus(401);
            return;
        }
        
        String username = (String) request.getSession().getAttribute(Login.SES_ATT_USERNAME);
        String attackeduser = request.getParameter(REQ_PAR_ATTACKED_USER);
        try
        {
            GameEngine.AttackStats attackResponse = GameEngine.attack(username, attackeduser);
            response.setContentType("text/json");
            JSONBuilder jb = new JSONBuilder();
            jb.beginObject();
            jb.property("tie", attackResponse.tie);
            jb.property("defenderLandTooLow", attackResponse.defenderLandTooLow);
            jb.property("attackerStrengthTooLow", attackResponse.attackerStrengthTooLow);
            jb.property("attackerEnergyTooLow", attackResponse.attackerEnergyTooLow);
            jb.property("result", attackResponse.result);
            jb.property("strength", attackResponse.strength);
            jb.property("land", attackResponse.land);
            jb.property("energy", attackResponse.energy);
            jb.endObject();
            response.getWriter().write(jb.toString());
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
